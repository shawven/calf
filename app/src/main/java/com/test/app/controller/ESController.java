package com.test.app.controller;

import lombok.Data;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.elasticsearch.search.aggregations.AggregationBuilders.avg;
import static org.elasticsearch.search.aggregations.AggregationBuilders.terms;
import static org.elasticsearch.search.aggregations.BucketOrder.aggregation;

/**
 * @author Shoven
 * @date 2019-08-06
 */
@RestController
@RequestMapping("es")
public class ESController {

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @GetMapping
    public Object index() {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery().must(termQuery("desc", "系统管理")))
                .build();

        searchQuery = new NativeSearchQueryBuilder()
                .withQuery(
                        matchQuery("name", "heiren")
                )
                .addAggregation(
                        terms("group_by_tags")
                                .field("tags")
                                .subAggregation(
                                        avg("price")
                                                .field("price")
                                )
                )
                .build();

        searchQuery = new NativeSearchQueryBuilder()
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

        return elasticsearchTemplate.queryForList(searchQuery, Article.class);
    }

    @PostMapping
    public Object create(Article article) {
        return null;
    }


    @Data
    @Document(indexName = "article", type = "_doc")
    public static class Article {

        private Integer num;

        private String user;

        private String desc;

        private String title;
    }
}
