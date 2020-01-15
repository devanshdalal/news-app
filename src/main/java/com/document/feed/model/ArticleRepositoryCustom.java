package com.document.feed.model;

import org.la4j.vector.dense.BasicVector;
import org.springframework.data.domain.PageRequest;
import reactor.core.publisher.Flux;

public interface ArticleRepositoryCustom {

  Flux<Article> findByDotProduct(BasicVector basicVector, PageRequest pageRequest);

  Flux<Article> findByProjection(PageRequest pageRequest);
}
