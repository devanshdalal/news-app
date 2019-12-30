package com.document.feed.model;

import reactor.core.publisher.Flux;

public interface TestingRepositoryCustom {
    Flux<Testing> findByDotProduct();
}
