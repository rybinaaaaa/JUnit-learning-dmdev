package com.rybina.service;

import com.rybina.paramResolver.UserServiceParamResolver;
import org.hamcrest.MatcherAssert;
import org.hamcrest.collection.IsMapContaining;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

//эта аннотация означает, что мы создаем лишь один тест-класс для всех тестов для юзера
@Tag("user")
@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {
    UserService userService;
    TestInfo testInfo;

    public UserServiceTest(TestInfo testInfo) {
        this.testInfo = testInfo;
        System.out.println(testInfo);
    }

    @BeforeAll
    void init() {
        System.out.println("Before All");
    }


//  ++ DI
    @BeforeEach
    void prepare(UserService userService) {
        System.out.println("Before Each");
        this.userService = userService;
    }

    @Test
    void UsersEmptyIfNoAdded() {
        var users = userService.getAll();

        assertThat(users, hasSize(0));
        assertThat(users, empty());
//        Assertions.assertTrue(user.isEmpty(), () -> "List should be empty");
    }

    @Test
    void UsesSizeIfUserAdded() {
        userService.add(new User("test1", "test1"));
        userService.add(new User("test2", "test2"));

        List<User> users = userService.getAll();

        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(0, users.get(0));
        userMap.put(1, users.get(1));


//        Надо делать так потому, что иначе после первого неверного Assertions юнит тест прекращается и не проверяет след Assertions
        assertAll(
                () -> MatcherAssert.assertThat(userMap, IsMapContaining.hasKey(0)),
                () -> MatcherAssert.assertThat(users, hasSize(2))
        );
    }

    @Test
    @Tag("login")
    void throwExceptionWhenUserPasswordIsNull() {
//         try {
//            userService.login("test", null);
//            fail("login should throw an exception when password is null");
//        } catch (IllegalArgumentException ex) {
//            assertTrue(true);
//        }

        assertAll(
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login("test", null)),
                () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "test"))
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
