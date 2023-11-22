package com.rybina.service;

import java.util.*;

public class UserService {
    private List<User> users = new ArrayList<>();

    public List<User> getAll() {
        return users;
    }

    public void add(User user) {
        users.add(user);
    }

    public Optional<User> login(String name, String password) {
        if(name == null || password == null) throw new IllegalArgumentException("username or password is null");
        return users.stream().filter(user -> Objects.equals(user.getName(), password) && Objects.equals(user.getPassword(), password)).findFirst();
    }
}
