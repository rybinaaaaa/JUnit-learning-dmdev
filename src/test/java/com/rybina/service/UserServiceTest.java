package com.rybina.service;

import org.junit.jupiter.api.*;

//эта аннотация означает, что мы создаем лишь один тест-класс для всех тестов для юзера
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserServiceTest {
    UserService userService;

    @BeforeAll
    void init() {
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
    void closeConnectionPool() {
        System.out.println("After All");
    }
}
