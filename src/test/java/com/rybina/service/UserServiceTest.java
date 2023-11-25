package com.rybina.service;

import com.rybina.TestBase;
import com.rybina.dao.UserDao;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.*;

import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserServiceTest extends TestBase {

    private static final User user1 = new User("test1", "test1", 1);
    private static final User user2 = new User("test2", "test2", 2);

    @Mock
    private UserDao userDao;

    @Captor
    ArgumentCaptor<Integer> argumentCaptor;

    @InjectMocks
    private UserService userService;

    @BeforeAll
    void init() {
        System.out.println("Before All");
    }


    //  ++ DI
    @BeforeEach
    void prepare() {
        System.out.println("Before Each");
//        this.userDao = Mockito.spy(UserDao.class);
//        this.userService = new UserService(userDao);
    }

    @Test
    @Order(1)
    void UsersEmptyIfNoAdded() {
        var users = userService.getAll();

        assertThat(users).hasSize(0);
        assertThat(users).isEmpty();
    }

    @Test
    void throwExceptionIfDatabaseIsNotAvailable() {
        doThrow(RuntimeException.class).when(userDao).delete(user1.getId());

        assertThrows(RuntimeException.class, () -> userService.delete(user1.getId()));
    }


    @Test
    void UsesSizeIfUserAdded() {
        userService.add(user1);
        userService.add(user2);

        List<User> users = userService.getAll();

        Map<Integer, User> userMap = new HashMap<>();
        userMap.put(0, users.get(0));
        userMap.put(1, users.get(1));

        assertAll(
                () -> assertThat(userMap).containsKey(0),
                () -> assertThat(users).hasSize(2)
        );
    }

    @Test
    void shouldDeleteExistingUser() {
        userService.add(user1);
//        doReturn(true).when(userDao).delete(user1.getId());

        BDDMockito.given(userDao.delete(user1.getId())).willReturn(true);
        BDDMockito.willReturn(true).given(userDao).delete(user1.getId());

        boolean deleteResult = userService.delete(user1.getId());

        Mockito.verify(userDao).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(user1.getId());
        assertThat(deleteResult).isTrue();
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
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    class LoginTest {
        @Test
        void loginSuccessIfUserExists() {
            userService.add(user1);
            Optional<User> user = assertTimeout(Duration.ofMillis(100L), () -> {
                return userService.login(user1.getName(), user1.getPassword());
            });
            assertThat(user).isPresent();
            user.ifPresent(u -> assertEquals(u, user1));
        }

        @RepeatedTest(name = RepeatedTest.LONG_DISPLAY_NAME, value = 5)
        void loginFailIfPasswordIncorrect() throws IOException {
            userService.add(user1);
            Optional<User> user = userService.login(user1.getName(), "dummy");
            assertThat(user).isEmpty();
        }

        @Test
        void loginFailIfNameIncorrect() {
            userService.add(user1);
            Optional<User> user = userService.login("dummy", user1.getPassword());
            assertThat(user).isEmpty();
        }

        @Test
        void throwExceptionWhenUserPasswordIsNull() {
            assertAll(
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login("test", null)),
                    () -> assertThrows(IllegalArgumentException.class, () -> userService.login(null, "test"))
            );
        }

        @ParameterizedTest(name = "{arguments} test")
        @MethodSource("com.rybina.service.UserServiceTest#getArgsForLoginTest")
        void loginParametrizedText(String name, String password, Optional<User> userToCompare) {
            userService.add(user1, user2);

            Optional<User> user = userService.login(name, password);
            assertThat(user).isEqualTo(userToCompare);
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
