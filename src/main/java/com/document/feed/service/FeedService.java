package com.document.feed.service;

import javax.servlet.http.HttpSession;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.document.feed.model.Article;
import com.document.feed.model.ArticleReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FeedService {
    private final ApplicationEventPublisher publisher;
    @Autowired
    private final ArticleReactiveRepository repository;

    public FeedService(ApplicationEventPublisher publisher,
            ArticleReactiveRepository repository) {
        this.publisher = publisher;
        this.repository = repository;
    }

    public Flux<Article> list() {
        System.out.println("/list called");
        System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
        return this.repository.findAll();
    }
}
