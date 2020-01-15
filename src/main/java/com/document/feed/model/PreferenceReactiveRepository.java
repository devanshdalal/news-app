package com.document.feed.model;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface PreferenceReactiveRepository extends ReactiveMongoRepository<Preference, String> {

  Flux<Preference> findByUsername(String username);
}
