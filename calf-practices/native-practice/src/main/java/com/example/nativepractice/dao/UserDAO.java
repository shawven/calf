package com.example.nativepractice.dao;

import com.example.nativepractice.entity.User;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author xw
 * @date 2022/11/25
 */
@Repository
public class UserDAO extends BaseDAOImpl<User> {

    public List<User> findAll() {
        return find(new Query());
    }
}
