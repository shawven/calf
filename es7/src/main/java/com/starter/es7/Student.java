package com.starter.es7;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDate;

@Data
@Document(indexName = "student")
public class Student {

    @Id
    private Long id;

    private String username;

    private String nickname;

    private Integer age;

    private byte sex;

    private float money;

    private LocalDate birthday;

    private String face;

    private String desc;
}
