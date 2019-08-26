package com.test.app.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * @author Shoven
 * @date 2019-07-11 15:21
 */
@Data
public class User implements Serializable {

    private String name;

    private Integer age;

    private String phone;
}
