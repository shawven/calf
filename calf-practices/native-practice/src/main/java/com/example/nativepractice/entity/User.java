package com.example.nativepractice.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author xw
 * @date 2022/11/25
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document("t_user")
public class User extends Entity{

    private String name;

    private int sex;
}
