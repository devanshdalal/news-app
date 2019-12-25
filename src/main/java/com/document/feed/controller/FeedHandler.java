package com.document.feed.controller;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

import javax.servlet.http.HttpSession;

import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.WebSession;

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

    Mono<ServerResponse> list(ServerRequest r) {
        /////;
        r.session().flatMap(webSession -> {
            System.out.println("webSession: " + webSession.toString());
            return null;
        });
        r.principal().flatMap(p -> {
            System.out.println("p:" + p);
            return null;
        });

        return defaultReadResponse(this.feedService.list());
    }

    @Bean
    public RouterFunction<ServerResponse> routeList() {
        return RouterFunctions
                .route(GET("/list").and(accept(MediaType.APPLICATION_JSON)),
                        this::list);
    }

    private static Mono<ServerResponse> defaultReadResponse(Publisher<Article> articlePublisher) {
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(articlePublisher, Article.class);
    }
}
