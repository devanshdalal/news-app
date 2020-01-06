package com.document.feed.model;

import static com.document.feed.util.GenericAggregationUtils.aggregate;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.util.FileCopyUtils;

import reactor.core.publisher.Flux;

public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {
    @Autowired
    ResourceLoader resourceLoader;
    private final ReactiveMongoTemplate mongoTemplate;
    private String project;

    public static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Autowired
    public ArticleRepositoryCustomImpl(ReactiveMongoTemplate mongoTemplate,
                                       ResourceLoader resourceLoader) {
        this.mongoTemplate = mongoTemplate;
        this.resourceLoader = resourceLoader;

        Resource resource= resourceLoader.getResource(
                "classpath:com/document/feed/model/project.txt");
        this.project = asString(resource);
    }

    public Flux<Article> findByDotProduct() {
        Aggregation aggregation = newAggregation(Testing.class,
                aggregate("$project", project),
                sort(Sort.Direction.DESC, "dot")
        );

        return mongoTemplate.aggregate(aggregation, "article", Article.class);
    }
}
