package com.document.feed.model;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import reactor.core.publisher.Flux;

@Repository
public interface TestingRepository extends ReactiveMongoRepository<Testing, String>, TestingRepositoryCustom {
}
