package com.rybina.service;

import com.rybina.paramResolver.UserServiceParamResolver;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

//эта аннотация означает, что мы создаем лишь один тест-класс для всех тестов для юзера
@Tag("user")
@Tag("fast")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.Random.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class) тесты вызываются в порядке, который мы задалив @Order(номер)
//@TestMethodOrder(MethodOrderer.MethodName.class) тесты вызываются в алфавитном порядке
//@TestMethodOrder(MethodOrderer.DisplayName.class) тесты вызываются в алфавитном порядке аннотаций, помеченных DisplayName
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {
    UserService userService;
    TestInfo testInfo;

    private static final User user1 = new User("test1", "test1", 1);
    private static final User user2 = new User("test2", "test2", 2);

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
//    @Order(0) для @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    @DisplayName("name") для MethodOrderer.DisplayName.class
    void UsersEmptyIfNoAdded() {
        var users = userService.getAll();

        assertThat(users).hasSize(0);
        assertThat(users).isEmpty();
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

    @AfterEach
    void deleteDataFromDataBase() {
        System.out.println("After Each");
    }

    @AfterAll
    void closeConnectionPool() {
        System.out.println("After All");
    }

    @Nested
    @Tag("login")
    class LoginTest {
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

        @ParameterizedTest(name = "{arguments} test")
//        Редко используемые
//        @ArgumentsSource() - мы должны передать сюда класс implements ArgumentsProvider
//        для NullSource, EmptySource, ValueSource, NullAndEmptySource допустим только один параметр!!!!
//        @NullSource - подставляет null в параметр
//        @EmptySource - подходит для массивов (в том числе для строк)
//        @NullAndEmptySource
//        @ValueSource(strings = {"name1", "name2"}) - по очереди вызовет фцию с name1 - 1 и с name2 - 2
//        @EnumSource

//        Часто используемый
//        @MethodSource("com.rybina.service.UserServiceTest#getArgsForLoginTest")

//        не можем передавать сложные данные в Csv
//        @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)
        @CsvSource({
                "test1, test1",
                "test2, test2"
        })
        void loginParametrizedText(String name, String password) {
            userService.add(user1, user2);

            Optional<User> user = userService.login(name, password);
            assertThat(user).isPresent();
        }
    }

    static Stream<Arguments> getArgsForLoginTest() {
        return Stream.of(
                Arguments.of(user1.getName(), user1.getPassword(), Optional.of(user1)),
                Arguments.of(user2.getName(), user2.getPassword(), Optional.of(user2)),
                Arguments.of(user2.getName(), "dummy", Optional.empty()),
                Arguments.of("dummy", user2.getPassword(), Optional.empty())
        );
    }
}
