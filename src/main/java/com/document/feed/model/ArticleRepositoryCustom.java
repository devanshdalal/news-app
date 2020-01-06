package com.document.feed.model;

import reactor.core.publisher.Flux;

public interface ArticleRepositoryCustom {
    Flux<Article> findByDotProduct();
}
