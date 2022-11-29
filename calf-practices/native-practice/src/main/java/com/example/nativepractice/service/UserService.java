package com.example.nativepractice.service;

import com.example.nativepractice.dao.UserDAO;
import com.example.nativepractice.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author xw
 * @date 2022/11/25
 */
@Service
public class UserService {

    @Autowired
    private UserDAO userDAO;

    public List<User> list() {
        return userDAO.findAll();
    }

    public void save(User user) {
        userDAO.save(user);
    }

    public void delete(String id) {
        userDAO.remove(id);
    }
}
