package com.document.feed.util;

import com.mongodb.assertions.Assertions;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

@Component
@Configuration
public class ApiKeyRoller {
  @Value("${newsapi.key}")
  private String[] apiKey;

  private int ind = -1;

  public void roll() {
    maybeInit();
    ind = (1 + ind) % apiKey.length;
  }

  public String get() {
    maybeInit();
    return apiKey[ind];
  }

  private void maybeInit() {
    if (ind == -1) {
      ind = 0;
//      ind = ThreadLocalRandom.current().nextInt(0, apiKey.length);
    }
  }
}
