package com.document.feed.model;

import static com.document.feed.util.GenericAggregationUtils.aggregate;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.la4j.vector.dense.BasicVector;
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

    public ArticleRepositoryCustomImpl(ReactiveMongoTemplate mongoTemplate,
            ResourceLoader resourceLoader) {
        this.mongoTemplate = mongoTemplate;
        this.resourceLoader = resourceLoader;

        Resource resource= resourceLoader.getResource(
                "classpath:com/document/feed/model/project.txt");
        this.project = asString(resource);
    }

    public static final List<String> fieldsToProject = new ArrayList<String>() {
        {
            add("id");
            add("author");
            add("title");
            add("description");
            add("content");
            add("url");
            add("publishedAt");
        }
    };


    private static String asString(Resource resource) {
        try (Reader reader = new InputStreamReader(resource.getInputStream())) {
            return FileCopyUtils.copyToString(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public Flux<Article> findByDotProduct(BasicVector basicVector) {
        List<String> fields =
                fieldsToProject.stream().map(x -> x + ": 1").collect(Collectors.toList());
        String finalProject = String.format(project,  String.join(",\n", fields),
                Arrays.toString(basicVector.toArray()));
        System.out.println("finalProject: " + finalProject);
        Aggregation aggregation = newAggregation(Article.class,
                aggregate("$project",
                        finalProject),
                        sort(Sort.Direction.DESC, "dot")
        );

        return mongoTemplate.aggregate(aggregation, "article", Article.class);
    }

    public Flux<Article> findByProjection() {
        System.out.println("Start findByProjection");
        Aggregation aggregation = newAggregation(Article.class,
                Aggregation.project(fieldsToProject.toArray(new String[0]))
        );

        return mongoTemplate.aggregate(aggregation, "article", Article.class);
    }
}
