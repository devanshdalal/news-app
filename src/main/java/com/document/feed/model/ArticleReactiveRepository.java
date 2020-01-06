package com.document.feed.model;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleReactiveRepository extends ReactiveMongoRepository<Article, String>, ArticleRepositoryCustom {
}
