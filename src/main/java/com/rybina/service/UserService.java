package com.rybina.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UserService {
    private List<User> users = new ArrayList<>();

    public List<User> getAll() {
        return users;
    }

    public void add(User user) {
        users.add(user);
    }
}
