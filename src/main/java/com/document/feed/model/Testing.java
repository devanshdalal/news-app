package com.document.feed.model;

import org.springframework.data.mongodb.core.mapping.Document;

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
public class Testing {
    int id;
    int myId;
    double score;
    int[] a;
    int dotProduct;

    public Testing(int id, int[] a, int dotProduct) {
        this.id = id;
        this.dotProduct = dotProduct;
        this.a = new int[a.length];
        for (int i = 0; i<a.length; ++i) {
            this.a[i] = a[i];
        }
    }
}

