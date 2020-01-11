package com.document.feed.service;

import java.util.List;
import java.util.Vector;
import java.util.stream.Stream;

import org.la4j.vector.dense.BasicVector;
import org.reactivestreams.Subscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.document.feed.model.Article;
import com.document.feed.model.ArticleReactiveRepository;
import com.document.feed.model.Preference;
import com.document.feed.model.PreferenceReactiveRepository;
import com.document.feed.model.UserReactiveRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class FeedService {
//    private Factory denseFactory;
    private final ApplicationEventPublisher publisher;
    @Autowired
    private final ArticleReactiveRepository articleRepository;

//    @Autowired
//    private final UserReactiveRepository userRepository;

    @Autowired
    private final PreferenceReactiveRepository preferenceRepository;

    public FeedService(ApplicationEventPublisher publisher,
                       ArticleReactiveRepository articleRepository,
                       UserReactiveRepository userRepository,
                       PreferenceReactiveRepository preferenceRepository) {
        this.publisher = publisher;
        this.articleRepository = articleRepository;
//        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
    }

    private Flux<Article> fetchByDotProduct(BasicVector basicVector) {
        return this.articleRepository.findByDotProduct(basicVector);
    }

    public Flux<Article> vanillaList() {
        Flux<Article> result = articleRepository.findByProjection();
        return result;
    }

    public Flux<Article> list() {
        System.out.println("/list called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        System.out.println("username:" + username);
        System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
        return this.preferenceRepository.findByUsername(username)
                .flatMap((Preference s) ->
                        Flux.just(new BasicVector(s.getArticle().getV())))
                .reduce( (v1, v2) -> (BasicVector) v1.add(v2))
                .flatMapMany(this::fetchByDotProduct)
                .switchIfEmpty(articleRepository.findByProjection());
    }

    public Flux<Article> getPreference() {
        System.out.println("/preference called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        System.out.println("username:" + username);
        System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
        return this.preferenceRepository.findByUsername(username)
                .flatMap((Preference s) -> Flux.just(s.getArticle()));
    }

    public Mono<Article> setPreference(String id, String username) {
        System.out.println("FeedService.setPreference called");
        return this.articleRepository.findById(id).flatMap(a ->
            preferenceRepository.save(new Preference(a, username))
                    .flatMap(saved -> Mono.just(saved.getArticle()))
        );
    }
}
