package com.document.feed.model;

import static com.fasterxml.jackson.annotation.JsonInclude.Include;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

  @Id private String id;

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
  private double[] v;

  // Extra fields computed during ordering.
  private double dot;

  public String getTitle() {
    return title;
  }

  public String getCountry() {
    return country;
  }

  public String getCategory() {
    return category;
  }

  public String getAuthor() {
    return author;
  }

  public String getDescription() {
    return description;
  }

  public String getContent() {
    return content;
  }

  public double[] getV() {
    return v;
  }

  public void setV(double[] v) {
    this.v = v;
  }

  public String getId() {
    return id;
  }
}
