package com.rybina.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertAll;

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
        var users = userService.getAll();

        Assertions.assertThat(users).hasSize(0);
        Assertions.assertThat(users).isEmpty();
//        Assertions.assertTrue(user.isEmpty(), () -> "List should be empty");
    }

    @Test
    void UsesSizeIfUserAdded() {
        userService.add(new User());
        userService.add(new User());

        List<User> users = userService.getAll();

        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(0, users.get(0));
        userMap.put(1, users.get(1));


//        Надо делать так потому, что иначе после первого неверного Assertions юнит тест прекращается и не проверяет след Assertions
        assertAll(
                () -> Assertions.assertThat(userMap).containsKey(0),
                () -> Assertions.assertThat(users).hasSize(2)
        );
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
