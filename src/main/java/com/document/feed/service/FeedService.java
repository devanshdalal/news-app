package com.document.feed.service;

import javax.servlet.http.HttpSession;

import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.document.feed.model.Article;
import com.document.feed.model.ArticleReactiveRepository;
import com.document.feed.model.UserReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FeedService {
    private final ApplicationEventPublisher publisher;
    @Autowired
    private final ArticleReactiveRepository repository;

    @Autowired
    private final UserReactiveRepository userReactiveRepository;

    public FeedService(ApplicationEventPublisher publisher,
                       ArticleReactiveRepository repository,
                       UserReactiveRepository userReactiveRepository) {
        this.publisher = publisher;
        this.repository = repository;
        this.userReactiveRepository = userReactiveRepository;
    }

    public Flux<Article> list(/*Authentication authentication*/) {
        System.out.println("/list called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        System.out.println("username:" + username);
//        userReactiveRepository.findByUsername(username).flatMap()
        System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
//        System.out.println("ac:" + authentication.toString());
        return this.repository.findAll();
    }
}
