package com.rybina.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UserServiceTest {

    @Test
    void UsersEmptyIfNoAdded() {
        UserService userService = new UserService();
        var user = userService.getAll();
        Assertions.assertTrue(user.isEmpty(), () -> "List should be empty");
    }
}
