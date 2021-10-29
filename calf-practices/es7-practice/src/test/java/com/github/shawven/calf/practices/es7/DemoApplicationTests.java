package com.github.shawven.calf.practices.es7;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.avg;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.BucketOrder.aggregation;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Test
    void f1() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("desc", "是"))
                .withPageable(PageRequest.of(0, 5))
                .build();

        SearchHits<Student> hits = elasticsearchRestTemplate.search(searchQuery, Student.class);
        hits.get().forEach(System.out::println);
    }

    @Test
    void f2() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("name", "heiren"))
                .addAggregation(
                        terms("group_by_tags")
                                .field("tags")
                                .subAggregation(
                                        avg("price")
                                                .field("price")
                                )
                )
                .build();

        SearchHits<Student> hits = elasticsearchRestTemplate.search(searchQuery, Student.class);
        hits.get().forEach(System.out::println);

    }

    @Test
    void f3() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(matchQuery("name", "heiren"))
                .addAggregation(
                        terms("group_by_tags")
                                .field("tags")
                                .order(
                                        aggregation("price", false)
                                )
                                .subAggregation(
                                        avg("price")
                                                .field("price")
                                )
                )
                .build();


        SearchHits<Student> hits = elasticsearchRestTemplate.search(searchQuery, Student.class);
        hits.get().forEach(System.out::println);
    }

}
