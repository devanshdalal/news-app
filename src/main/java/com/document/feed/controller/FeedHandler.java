package com.document.feed.controller;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.ServerRequest.Headers;

import com.document.feed.model.Article;
import com.document.feed.service.FeedService;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import io.netty.handler.codec.http.HttpHeaders;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SignalType;

@Component
@Configuration
public class FeedHandler {

  private final FeedService feedService;

  private final String baseUrl = "http://newsapi.org";

  private final WebClient client = WebClient.builder().baseUrl(baseUrl).build();

  @Value("${newsapi.key}")
  private String apiKey;

  FeedHandler(FeedService feedService) {
    this.feedService = feedService;
  }

  private static Mono<ServerResponse> defaultReadResponse(Publisher<Article> articlePublisher) {
    return ServerResponse.ok().contentType(APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
        .body(articlePublisher, Article.class);
  }

  @Bean
  public RouterFunction<ServerResponse> routeVanillaList() {
    return RouterFunctions.route(OPTIONS("/**").and(accept(ALL)), this::HandleOptionsCall)
        .andRoute(GET("/vanillalist").and(accept(APPLICATION_JSON)), this::vanillaList)
        .andRoute(GET("/list").and(accept(APPLICATION_JSON)), this::list)
        .andRoute(GET("/liked").and(accept(APPLICATION_JSON)), this::getPreference)
        .andRoute(POST("/like").and(accept(APPLICATION_JSON)), this::setPreference)
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

  Mono<ServerResponse> newsAPI(ServerRequest r) {
    System.out.println("r.uri(): " + r.uri() + "-" + r.path());
    // /newsapi/v2/top-headlines?page=1&pageSize=30&language=en =>
    // v2/top-headlines?page=1&pageSize=30&language=en
    String path = r.path().substring(8);
    WebClient.ResponseSpec response = client.get()
        .uri(uriBuilder -> uriBuilder.path(path).queryParam("apiKey", apiKey).queryParams(r.queryParams()).build())
        .retrieve();
    return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).header("Access-Control-Allow-Origin", "*")
        .body(response.bodyToMono(String.class), String.class);
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
    return item.flatMap(objectId -> {
      System.out.println("objectId: " + objectId);
      return this.feedService.deletePreference(objectId)
          .then(ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(objectId));
    });
  }

  Mono<ServerResponse> getPreference(ServerRequest r) {
    r.session().subscribe(webSession -> System.out.println("webSession:" + webSession.getCreationTime()));
    return defaultReadResponse(this.feedService.getPreference());
  }

  public Mono<ServerResponse> setPreference(ServerRequest request) {
    System.out.println("Start setPreference()");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();
    Mono<Article> item = request.bodyToMono(Article.class);
    return item.flatMap(i -> {
      i.setV(RunCmd(i));
      return Mono.just(i);
    }).flatMap(objectId -> this.feedService.setPreference(objectId, username)
        .flatMap(x -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(x))
        .switchIfEmpty(ServerResponse.notFound().build()));
  }

  private double[] RunCmd(Article item) {
    String lastLine = "";
    try {
      String cmd = "python nlp/preference_saver.py";
      // cmd += " " + item.getCountry();
      // cmd += " " + item.getCategory();
      cmd += " \"" + item.getAuthor() + "\"";
      cmd += " \"" + item.getTitle();
      cmd += " " + item.getDescription();
      cmd += " " + item.getContent() + "\"";
      cmd = cmd.replace('\n', ' ');
      System.out.println("cmd: " + cmd);
      Process process = Runtime.getRuntime().exec(cmd);
      StringBuilder output = new StringBuilder();

      BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n"); // just get the last line
        lastLine = line;
      }

      int exitVal = process.waitFor();
      System.out.println(output);
      if (exitVal == 0) {
        System.out.println("Success!");
      } else {
        System.out.println("Failure!");
      }
    } catch (IOException | InterruptedException e) {
      e.printStackTrace();
    }
    String[] weightArray = lastLine.split(" ");
    double[] ret = new double[weightArray.length];
    int index = 0;
    for (String wt : weightArray) {
      ret[index++] = Double.parseDouble(wt);
    }
    return ret;
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
