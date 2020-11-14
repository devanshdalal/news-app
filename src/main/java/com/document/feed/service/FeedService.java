package com.document.feed.service;

import com.document.feed.model.Article;
import com.document.feed.model.ArticleReactiveRepository;
import com.document.feed.model.Preference;
import com.document.feed.model.PreferenceReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.la4j.vector.dense.BasicVector;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class FeedService {

  private final ArticleReactiveRepository articleRepository;

  private final PreferenceReactiveRepository preferenceRepository;

  public Flux<Article> vanillaList(PageRequest pageRequest) {
    return articleRepository.findByProjection(pageRequest);
  }

  public Flux<Article> list(PageRequest pageRequest) {
    System.out.println("/list called");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();
    System.out.println("username:" + username);
    System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
    return this.preferenceRepository
        .findByUsername(username)
        .flatMap(s -> Flux.just(new BasicVector(s.getArticle().getV())))
        .reduce((v1, v2) -> (BasicVector) v1.add(v2))
        .flatMapMany(v -> fetchByDotProduct(v, pageRequest))
        .switchIfEmpty(articleRepository.findByProjection(pageRequest));
  }

  private Flux<Article> fetchByDotProduct(BasicVector basicVector, PageRequest pageRequest) {
    return this.articleRepository.findByDotProduct(basicVector, pageRequest);
  }

  public Flux<Article> getPreference() {
    System.out.println("/preference called");
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    String username = (String) authentication.getPrincipal();
    System.out.println("username:" + username);
    System.out.println("sc:" + SecurityContextHolder.getContext().getAuthentication());
    return this.preferenceRepository
        .findByUsername(username)
        .flatMap(s -> Flux.just(s.getArticle()));
  }

  public Mono<Preference> savePreference(Article a, String username) {
    System.out.println("FeedService.savePreference called");
    return preferenceRepository
        .save(new Preference(a, username))
        .flatMap(Mono::just);
  }

  public Mono<Void> deletePreference(String id) {
    System.out.println("FeedService.deletePreference called");
    return preferenceRepository.deleteById(id);
  }
}
