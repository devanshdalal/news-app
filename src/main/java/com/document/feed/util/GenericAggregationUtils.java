package com.document.feed.util;

import com.mongodb.DBObject;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;

public interface GenericAggregationUtils {

  static AggregationOperation aggregate(String operation, String query) {
    return new GenericAggregationOperation(operation, query);
  }

  static AggregationOperation aggregate(String operation, DBObject query) {
    return new GenericAggregationOperation(operation, query);
  }
}
