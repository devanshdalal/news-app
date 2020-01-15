package com.document.feed.controller;

import static org.springframework.http.MediaType.ALL;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.OPTIONS;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.document.feed.model.Article;
import com.document.feed.service.FeedService;
import reactor.core.publisher.Mono;

@Component
@Configuration
public class FeedHandler {

    private final FeedService feedService;

    FeedHandler(FeedService feedService) {
        this.feedService = feedService;
    }

    @Bean
    public RouterFunction<ServerResponse> routeVanillaList() {
        return RouterFunctions
                .route(OPTIONS("/**").and(accept(ALL)),
                        this::HandleOptionsCall)
                .andRoute(GET("/vanillalist").and(accept(APPLICATION_JSON)),
                        this::vanillaList)
                .andRoute(GET("/list").and(accept(APPLICATION_JSON)),
                        this::list)
                .andRoute(GET("/liked").and(accept(APPLICATION_JSON)),
                        this::getPreference)
                .andRoute(POST("/like").and(accept(APPLICATION_JSON)),
                        this::setPreference);
    }

    Mono<ServerResponse> HandleOptionsCall(ServerRequest r) {
        return ServerResponse
                .ok().bodyValue("OK");
    }

    Mono<ServerResponse> list(ServerRequest r) {
        return defaultReadResponse(this.feedService.list());
    }

    Mono<ServerResponse> vanillaList(ServerRequest r) {
        return defaultReadResponse(this.feedService.vanillaList());
    }

    private static Mono<ServerResponse> defaultReadResponse(Publisher<Article> articlePublisher) {
        return ServerResponse
                .ok()
                .contentType(APPLICATION_JSON)
                .body(articlePublisher, Article.class);
    }

    Mono<ServerResponse> getPreference(ServerRequest r) {
        r.session().subscribe(webSession -> System.out.println("webSession:" + webSession.getCreationTime()));
        return defaultReadResponse(this.feedService.getPreference());
    }

    public Mono<ServerResponse> setPreference(ServerRequest request) {
        System.out.println("Start setPreference()");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        Mono<String> id = request.bodyToMono(String.class);
        return id.flatMap(objectId -> HandlePreferenceCall(objectId, username));
    }

    private Mono<ServerResponse> HandlePreferenceCall(String objectId, String username) {
        System.out.println("Start HandlePreferenceCall");
        Mono<ServerResponse> notFound = ServerResponse.notFound().build();
        return this.feedService.setPreference(objectId, username)
                .flatMap(x -> ServerResponse.ok().contentType(APPLICATION_JSON).bodyValue(x))
                .switchIfEmpty(notFound);
    }
}
