package com.rybina.service;

import com.rybina.dao.UserDao;

import java.util.*;

public class UserService {
    private List<User> users = new ArrayList<>();
    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public boolean delete(Integer id) {
        return userDao.delete(id);
    }

    public List<User> getAll() {
        return users;
    }

    public Optional<User> login(String name, String password) {
        if (name == null || password == null) throw new IllegalArgumentException("username or password is null");
        return users.stream().filter(user -> Objects.equals(user.getName(), name) && Objects.equals(user.getPassword(), password)).findFirst();
    }

    public void add(User... users) {
        this.users.addAll(List.of(users));
    }


}
