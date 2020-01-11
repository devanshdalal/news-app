package com.document.feed.model;

import org.la4j.vector.dense.BasicVector;

import reactor.core.publisher.Flux;

public interface ArticleRepositoryCustom {
    Flux<Article> findByDotProduct(BasicVector basicVector);
    Flux<Article> findByProjection();
}
