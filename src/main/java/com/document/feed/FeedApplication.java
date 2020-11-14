package com.document.feed;

import com.document.feed.model.ArticleReactiveRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@RequiredArgsConstructor
@EnableRetry
public class FeedApplication implements CommandLineRunner {

  private final ArticleReactiveRepository repository;

  public static void main(String[] args) {
    SpringApplication.run(FeedApplication.class, args);
  }

  @Override
  public void run(String[] args) {
    //		System.out.println("Start run");
    //		final Article a = new Article(new ObjectId("5df4a715e34951bc73f810ee"),
    //				null,
    //				"author",
    //				"tittle",
    //				"desc",
    //				"url",
    //				"url2",
    //				"p",
    //				"content",
    //				"category");
    //		repository.save(a).subscribe();
    //		repository.findAll()
    //				.log()
    //				.subscribe(System.out::println);
    //		System.out.println("End run");
  }
}
