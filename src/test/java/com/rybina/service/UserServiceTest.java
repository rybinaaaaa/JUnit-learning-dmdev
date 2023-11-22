package com.rybina.service;

import org.junit.jupiter.api.*;

class UserServiceTest {
    UserService userService;

    @BeforeAll
    static void init() {
        System.out.println("Before All");
    }


    @BeforeEach
    void prepare() {
        System.out.println("Before each");
        userService = new UserService();
    }

    @Test
    void UsersEmptyIfNoAdded() {
        var user = userService.getAll();

        Assertions.assertTrue(user.isEmpty(), () -> "List should be empty");
    }

    @Test
    void UsesSizeIfUserAdded() {
        userService.add(new User());
        userService.add(new User());

        var users = userService.getAll();
        Assertions.assertEquals(2, users.size());
    }

    @AfterEach
    void deleteDataFromDataBase() {
        System.out.println("After Each");
    }

    @AfterAll
    static void closeConnectionPool() {
        System.out.println("After All");
    }
}
