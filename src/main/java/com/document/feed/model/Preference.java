package com.document.feed.model;

import java.util.stream.Stream;

import org.reactivestreams.Publisher;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Preference {
    @Id
    private String id;
    private Article article;
    private String username;

    public Preference(Article article, String username) {
        this.article = article;
        this.username = username;
    }

    public Article getArticle() {
        return article;
    }
}