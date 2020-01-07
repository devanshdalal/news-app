package com.document.feed.service;

import java.util.Vector;
import java.util.stream.Stream;

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

@Service
public class FeedService {
    private final ApplicationEventPublisher publisher;
    @Autowired
    private final ArticleReactiveRepository articleRepository;

    @Autowired
    private final UserReactiveRepository userRepository;

    @Autowired
    private final PreferenceReactiveRepository preferenceRepository;

    public FeedService(ApplicationEventPublisher publisher,
                       ArticleReactiveRepository articleRepository,
                       UserReactiveRepository userRepository,
                       PreferenceReactiveRepository preferenceRepository) {
        this.publisher = publisher;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
        this.preferenceRepository = preferenceRepository;
    }

    public Flux<Article> list(/*Authentication authentication*/) {
        System.out.println("/list called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = (String) authentication.getPrincipal();
        System.out.println("username:" + username);
//        userReactiveRepository.findByUsername(username).flatMap()
        System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
//        System.out.println("ac:" + authentication.toString());
        this.preferenceRepository.findByUsername(username)
                .flatMap((Preference s) -> Flux.just(s.getArticle().getV()));
//                .reduce();
        

        return this.articleRepository.findByDotProduct();
    }
}
