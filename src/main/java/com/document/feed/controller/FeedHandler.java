package com.document.feed.controller;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import com.document.feed.model.Article;
import com.document.feed.service.FeedService;
import com.document.feed.util.ApiKeyRoller;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Configuration
@RequiredArgsConstructor
public class FeedHandler {

  private final FeedService feedService;

  private final ApiKeyRoller apiKeyRoller;

  @Value("${spring.data.mongodb.uri}")
  private String mongoUrl;

  private final String baseUrl = "http://newsapi.org";

  private final WebClient client =
      WebClient.builder().filter(perResponse()).baseUrl(baseUrl).build();

  private static Mono<ServerResponse> defaultReadResponse(Publisher<Article> articlePublisher) {
    return ServerResponse.ok()
        .contentType(APPLICATION_JSON)
        .header("Access-Control-Allow-Origin", "*")
        .body(articlePublisher, Article.class);
  }

  private ExchangeFilterFunction perResponse() {
    return ExchangeFilterFunction.ofResponseProcessor(
        clientResponse -> {
          clientResponse
              .headers()
              .asHttpHeaders()
              .forEach((h, g) -> System.out.println(h + " g: " + g));
          return Mono.just(clientResponse);
        });
  }

  @Bean
  public RouterFunction<ServerResponse> routeVanillaList() {
    return RouterFunctions.route(OPTIONS("/**").and(accept(ALL)), this::HandleOptionsCall)
        .andRoute(GET("/vanillalist").and(accept(APPLICATION_JSON)), this::vanillaList)
        .andRoute(GET("/list").and(accept(APPLICATION_JSON)), this::list)
        .andRoute(GET("/liked").and(accept(APPLICATION_JSON)), this::getPreference)
        .andRoute(POST("/like").and(accept(APPLICATION_JSON)), this::savePreference)
        .andRoute(POST("/dislike").and(accept(APPLICATION_JSON)), this::deletePreference)
        .andRoute(GET("/newsapi/**").and(accept(APPLICATION_JSON)), this::newsAPI);
  }

  Mono<ServerResponse> HandleOptionsCall(ServerRequest r) {
    return ServerResponse.ok().bodyValue("OK");
  }

  Mono<ServerResponse> vanillaList(ServerRequest r) {
    PageRequest pageRequest = createPageRequest(r);
    return defaultReadResponse(this.feedService.vanillaList(pageRequest));
  }

  //  public Mono<ServerResponse> handleRequest(ServerRequest request) {
  //    return sayHello(request)
  //        .flatMap(s -> ServerResponse.ok()
  //            .contentType(MediaType.TEXT_PLAIN)
  //            .bodyValue(s))
  //        .onErrorResume(e -> Mono.just("Error " + e.getMessage())
  //            .flatMap(s -> ServerResponse.ok()
  //                .contentType(MediaType.TEXT_PLAIN)
  //                .bodyValue(s)));
  //  }

  private ResponseSpec getResponseSpec(String path, MultiValueMap<String, String> queryParams) {
    return client
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .path(path)
                    .queryParam("apiKey", apiKeyRoller.get())
                    .queryParams(queryParams)
                    .build())
        .retrieve();
  }

  @Retryable(value = Exception.class)
  Mono<ServerResponse> newsAPI(ServerRequest r) {
    var cacheControl = CacheControl.maxAge(30, TimeUnit.MINUTES);
    System.out.println("r.uri(): " + r.uri() + "-" + r.path());
    // /newsapi/v2/top-headlines?page=1&pageSize=30&language=en =>
    // v2/top-headlines?page=1&pageSize=30&language=en
    String path = r.path().substring(8);
    return ServerResponse.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .header("Access-Control-Allow-Origin", "*")
        .cacheControl(cacheControl)
        .body(
            getResponseSpec(path, r.queryParams())
                .bodyToMono(String.class)
                .doOnError(this::maybeRollKey)
                .onErrorResume(
                    e -> getResponseSpec(path, r.queryParams()).bodyToMono(String.class)),
            String.class);
  }

  private void maybeRollKey(Throwable throwable) {
    System.out.println("Throw: " + throwable.getMessage());
    if (throwable instanceof WebClientResponseException
        && ((WebClientResponseException) throwable).getStatusCode().value() == 429) {
      apiKeyRoller.roll();
    }
    System.out.println("key: " + apiKeyRoller.get());
  }

  Mono<ServerResponse> list(ServerRequest r) {
    PageRequest pageRequest = createPageRequest(r);
    return defaultReadResponse(this.feedService.list(pageRequest));
  }

  public Mono<ServerResponse> deletePreference(ServerRequest request) {
    System.out.println("Start deletePreference()");
    // Authentication authentication =
    // SecurityContextHolder.getContext().getAuthentication();
    // String username = (String) authentication.getPrincipal();
    Mono<String> item = request.bodyToMono(String.class);
    return item.flatMap(
        objectId -> {
          System.out.println("objectId: " + objectId);
          return this.feedService
              .deletePreference(objectId)
              .then(ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(objectId));
        });
  }

  Mono<ServerResponse> getPreference(ServerRequest r) {
    r.session()
        .subscribe(webSession -> System.out.println("webSession:" + webSession.getCreationTime()));
    return defaultReadResponse(this.feedService.getPreference());
  }

  public Mono<ServerResponse> savePreference(ServerRequest request) {
    System.out.println("Start savePreference()");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();
    Mono<Article> item = request.bodyToMono(Article.class);
    return item.flatMap(
            article ->
                this.feedService
                    .savePreference(article, username)
                    .doOnSuccess(
                        preference -> {
                          System.out.println("serverResponse: " + preference);
                          RunCmd(preference.getId());
                        }))
        .flatMap(x -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(x))
        .switchIfEmpty(ServerResponse.notFound().build());
  }

  private void RunCmd(String id) {
    try {
//      String cmd = "python nlp/preference_saver.py " + id;
      String cmd = "python nlp/preference_saver.py \"" + mongoUrl +"\" "+ id;
      System.out.println("cmd: " + cmd);
      Process process = Runtime.getRuntime().exec("python --version");
      StringBuilder output = new StringBuilder();

      ProcessBuilder processBuilder = new ProcessBuilder();

      int exitVal = process.waitFor();
      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
        System.out.println("line: " + line);
      }

      System.out.println("output: " + output);
      if (exitVal == 0) {
        System.out.println("Success!");
      } else {
        System.out.println("Failure!");
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
  }

  private PageRequest createPageRequest(ServerRequest r) {
    MultiValueMap<String, String> params = r.queryParams();
    System.out.println("headers " + r.queryParams());

    int pageSize = 30; // default
    List<String> pageSizeOpt = params.get("limit");
    if (null != pageSizeOpt && !pageSizeOpt.isEmpty() && !pageSizeOpt.isEmpty()) {
      pageSize = Integer.parseInt(pageSizeOpt.get(0));
    }

    int pageIndex = 0; // default
    List<String> skipOpt = params.get("skip");
    if (null != skipOpt && !skipOpt.isEmpty() && !skipOpt.get(0).isEmpty()) {
      // TODO(devansh): client can't request page starting from offset 10 of pagesize
      // 500. Please handle that.
      pageIndex = Integer.parseInt(skipOpt.get(0)) / pageSize;
    }

    return PageRequest.of(pageIndex, pageSize);
  }
}
