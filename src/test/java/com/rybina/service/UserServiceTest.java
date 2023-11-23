package com.rybina.service;

import com.rybina.paramResolver.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

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

    private static final User user1 = new User("test1", "test1", 1);
    private static final User user2 = new User("test1", "test1", 2);

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

        assertThat(users).hasSize(0);
        assertThat(users).isEmpty();
//        Assertions.assertTrue(user.isEmpty(), () -> "List should be empty");
    }

    @Test
    void UsesSizeIfUserAdded() {
        userService.add(user1);
        userService.add(user2);

        List<User> users = userService.getAll();

        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(0, users.get(0));
        userMap.put(1, users.get(1));


//        Надо делать так потому, что иначе после первого неверного Assertions юнит тест прекращается и не проверяет след Assertions
        assertAll(
                () -> assertThat(userMap).containsKey(0),
                () -> assertThat(users).hasSize(2)
        );
    }

    @Test
    @Tag("login")
    void loginSuccessIfUserExists() {
        userService.add(user1);
        Optional<User> user = userService.login(user1.getName(), user1.getPassword());
        assertThat(user).isPresent();
        user.ifPresent(u -> assertEquals(u, user1));
    }

    @Test
    @Tag("login")
    void loginFailIfPasswordIncorrect() {
        userService.add(user1);
        Optional<User> user = userService.login(user1.getName(), "dummy");
        assertThat(user).isEmpty();
    }

    @Test
    @Tag("login")
    void loginFailIfNameIncorrect() {
        userService.add(user1);
        Optional<User> user = userService.login("dummy", user1.getPassword());
        assertThat(user).isEmpty();
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
