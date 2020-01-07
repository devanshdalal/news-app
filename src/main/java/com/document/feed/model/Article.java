package com.document.feed.model;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNullFields;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
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
class Source {
    private String id;
    private String name;
}

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class Article {

    @Id
    private String id;
    private Source source;
    private String author;
    private String title;
    private String description;
    private String url;
    private String urlToImage;
    private String publishedAt;
    private String content;
    private String country;
    private String category;

    // Vector of Tf-Idf weights.
    private List<Double> v;

    // Extra fields computed during ordering.
    private double dot;

//    public Article(String id, String author, Double dot) {
//        this.id = id;
//        this.author = author;
//        this.dot = dot;
//    }
    public List<Double> getV() {
        return v;
    }
}
