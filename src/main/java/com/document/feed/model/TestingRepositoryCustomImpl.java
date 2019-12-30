package com.document.feed.model;

import static com.document.feed.util.GenericAggregationUtils.aggregate;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationExpression;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.repository.Query;

import reactor.core.publisher.Flux;

public class TestingRepositoryCustomImpl implements TestingRepositoryCustom {
    private final ReactiveMongoTemplate mongoTemplate;

    @Autowired
    public TestingRepositoryCustomImpl(ReactiveMongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public Flux<Testing> findByDotProduct() {
        Aggregation aggregation = newAggregation(Testing.class,
                        aggregate("$project", "{ a: 1, dotProduct: { $reduce: { input: { $range: "
                                + "[ 0, { $size: \"$a\" }] }, initialValue: 0, in: { $let: { vars: { b: [3,2,1] }, in: { $add: [ \"$$value\", { $multiply: [ { $arrayElemAt: [ \"$a\", \"$$this\" ] }, { $arrayElemAt: [ \"$$b\", \"$$this\" ] } ] } ] } } } } } }"),
                        sort(Sort.Direction.DESC, "$dotProduct")
        );

        return mongoTemplate.aggregate(aggregation, "testing", Testing.class);
    }
}
