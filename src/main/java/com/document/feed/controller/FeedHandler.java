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
import java.util.List;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Configuration
public class FeedHandler {

  private final FeedService feedService;

  FeedHandler(FeedService feedService) {
    this.feedService = feedService;
  }

  private static Mono<ServerResponse> defaultReadResponse(Publisher<Article> articlePublisher) {
    return ServerResponse.ok().contentType(APPLICATION_JSON)
        .body(articlePublisher, Article.class);
  }

  @Bean
  public RouterFunction<ServerResponse> routeVanillaList() {
    return RouterFunctions.route(OPTIONS("/**").and(accept(ALL)), this::HandleOptionsCall)
        .andRoute(GET("/vanillalist").and(accept(APPLICATION_JSON)), this::vanillaList)
        .andRoute(GET("/list").and(accept(APPLICATION_JSON)), this::list)
        .andRoute(GET("/liked").and(accept(APPLICATION_JSON)), this::getPreference)
        .andRoute(POST("/like").and(accept(APPLICATION_JSON)), this::setPreference);
  }

  Mono<ServerResponse> HandleOptionsCall(ServerRequest r) {
    return ServerResponse.ok().bodyValue("OK");
  }

  Mono<ServerResponse> vanillaList(ServerRequest r) {
    PageRequest pageRequest = createPageRequest(r);
    return defaultReadResponse(this.feedService.vanillaList(pageRequest));
  }

  Mono<ServerResponse> list(ServerRequest r) {
    PageRequest pageRequest = createPageRequest(r);
    return defaultReadResponse(this.feedService.list(pageRequest));
  }

  Mono<ServerResponse> getPreference(ServerRequest r) {
    r.session().subscribe(
        webSession -> System.out.println("webSession:" + webSession.getCreationTime()));
    return defaultReadResponse(this.feedService.getPreference());
  }

  public Mono<ServerResponse> setPreference(ServerRequest request) {
    System.out.println("Start setPreference()");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();
    Mono<String> id = request.bodyToMono(String.class);
    return id.flatMap(objectId -> this.feedService.setPreference(objectId, username)
        .flatMap(x -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(x))
        .switchIfEmpty(ServerResponse.notFound().build())).doFinally(i -> RunCmd());
  }

  private void RunCmd() {
    try {
      Process process = Runtime.getRuntime().exec("python nlp/main.py True");
      StringBuilder output = new StringBuilder();

      BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream()));

      String line;
      while ((line = reader.readLine()) != null) {
        output.append(line + "\n");
      }

      int exitVal = process.waitFor();
      if (exitVal == 0) {
        System.out.println("Success!");
        System.out.println(output);
        System.exit(0);
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

    int pageSize = 30;  // default
    List<String> pageSizeOpt = params.get("limit");
    if (null != pageSizeOpt && !pageSizeOpt.isEmpty() && !pageSizeOpt.isEmpty()) {
      pageSize = Integer.parseInt(pageSizeOpt.get(0));
    }

    int pageIndex = 0;  // default
    List<String> skipOpt = params.get("skip");
    if (null != skipOpt && !skipOpt.isEmpty() && !skipOpt.get(0).isEmpty()) {
      // TODO(devansh): client can't request page starting from offset 10 of pagesize 500. Please handle that.
      pageIndex = Integer.parseInt(skipOpt.get(0)) / pageSize;
    }

    return PageRequest.of(pageIndex, pageSize);
  }
}
