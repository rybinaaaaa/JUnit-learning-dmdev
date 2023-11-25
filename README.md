# JUnit-learning-dmdev

# #testing
[rybinaaaaa/JUnit-learning-dmdev](https://github.com/rybinaaaaa/JUnit-learning-dmdev)
## 0. Intro

> **TDD** - метод писания кода, когда ты сначала пишешь тесты на несуществующую функцию, которые она должна будет проходить, а потом создаешь этот метод

![](image.png)<!-- {"width":463} -->

> **Unit test** - тестирование одной единой функции без взаимодействия с другими классами

> **Integration test** - тестирование функции, которая как либо взаимодействует с другими классами, то есть мы проверяем уже функционал нескольких классов

> **Acceptance testing** - тестирование всего приложения в целом, т.е. как оно работает со стороны пользователя (функциональное тестирование)

## 1. Юнит тесты

> Юнит тесты - тестируют одну функцию изолируя от другого функционала

### 1.1. Lifecycle

BeforeAll -> BeforeEach -> Testing -> AfterEach -> AfterAll

**Важно заметить, что *BeforeAll* и *AfterAll* в силу своей реализации обязывают нас ставить модификатор *static***. Если мы хотим убрать этот момент, мы должны добавить аннотацию **@TestInstance(TestInstance.Lifecycle.PER_CLASS)**. **по умолчанию стоит TestInstance.Lifecycle.PER_Method**
```
//эта аннотация означает, что мы создаем лишь один тест-класс для всех тестов для юзера
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserServiceTest {
```

## 2. Launcher

> Launcher - инструмент, с помощью которого maven и gradle запускают тесты

![](Screenshot%202023-11-22%20at%2019.24.44.png)

Пример:

```
public class TestLauncher {

    public static void main(String[] args) {
        Launcher launcher = LauncherFactory.create();

//        launcher.registerLauncherDiscoveryListeners();
//        launcher.registerTestExecutionListeners();

        var summaryGeneratedListener = new SummaryGeneratingListener(); //дает статистику по пройденым тестам

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
//              .selectors(DiscoverySelectors.selectClass(UserServiceTest.class))

                .selectors(DiscoverySelectors.selectPackage("com.rybina.service"))

//              .listeners()
                .build();
        launcher.execute(request, summaryGeneratedListener);

        try(var writer = new PrintWriter(System.out)) {
            summaryGeneratedListener.getSummary().printTo(writer);
        }
    }
}
```

Объяснение: 
- LauncherFactory.create() - возвращает дефолтный лаунчер
- launcher.registerLauncherDiscoveryListeners - регистрирует прослушиватели, **если у нас прослушали объявлены и в LauncherDiscoveryRequestBuilder и в лауничере, то прослушивали будут суммироваться**
- launcher.registerTestExecutionListeners - слушатели, которые обрабатывают исход теста, их можно передать  и в конце в метод **execute**
- ! **launcher.execute**   возвращает null, вся информация записывается в **TestExecutionListeners**
- selectors - критерии по которым лаунчер вообще находит классы, которые надо тестировать

*Необходимые зависимости:*

```
<dependency>
    <groupId>org.junit.platform</groupId>
    <artifactId>junit-platform-launcher</artifactId>
    <version>1.8.0-M1</version>
    <scope>test</scope>
</dependency>

```

## 3. AssertJ & Hamcrest

> AssertJ - библиотека, которая позволяет делать более абстрактные и читабельные Assertions

```
    @Test
    void UsersEmptyIfNoAdded() {
        var users = userService.getAll();

        Assertions.assertThat(users).hasSize(0);
        Assertions.assertThat(users).isEmpty();
//        Assertions.assertTrue(user.isEmpty());
    }

```

**Assertions.assertThat также сравнивает данные используя методы возвращаемых данных**, например

```
List<User> users = userService.getAll();

Map<Integer, User> userMap = new HashMap<>();
userMap.put(0, users.get(0));
userMap.put(1, users.get(1));

Assertions.assertThat(userMap).containsKey(0);

Assertions.assertThat(users).hasSize(2);
```

### Перепишем это на Harmcrest

```
assertAll(
        () -> MatcherAssert.assertThat(userMap, IsMapContaining.hasKey(0)),
        () -> MatcherAssert.assertThat(users, hasSize(2))
);
```

### 4. Assertions.assertAll

> assertAll - еквивалентен runnable, но он throw Exception, поэтому если у нас не один assert в юнит тесте, то лучше использовать его

```
//        Надо делать так потому, что иначе после первого неверного Assertions юнит тест прекращается и не проверяет след Assertions
        assertAll(
                () -> Assertions.assertThat(userMap).containsKey(0),
                () -> Assertions.assertThat(users).hasSize(2)
        );
```

## 5. Exceptions

Для Exceptions мы используем **assertThrows**. 
- fail("login should throw an exception when password is null"); - пробрасывает исключение

```
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
```

## 6. Tagging
Мы можем добавлять @Tag нашим методам (и классу). 
Потом в лаунчере мы можем **фильтровать выполнение тестов в зависимости от тегов**

```
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder
                .request()
//              .selectors(DiscoverySelectors.selectClass(UserServiceTest.class))
                .selectors(DiscoverySelectors.selectPackage("com.rybina.service"))
//                .listeners()
                .filters(
                        TagFilter.includeTags("login")
                )
                .build();
        launcher.execute(request, summaryGeneratedListener);
```

-----

```
TagFilter.includeTags("login")
```

— выполнится все, что имеет @Tag(“login”) 

```
TagFilter.excludeTags("login")
```

— выполнится все, что не имеет @Tag(“login”)

## 7. DI в JUnit

Для того, чтобы мы брали из какого-то контейнера сущности, которые привязываем к классу, нам надо создать класс **ParameterResolver**

```
public class UserServiceParamResolver implements ParameterResolver {
    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == UserService.class ; -- смотрим есть ли у нас в аргументах класс, который мы хоти вставлять при необходимости
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        кеширование
//        Namespace - ключ для стора. раскрываем как hashmap
        var store = extensionContext.getStore(ExtensionContext.Namespace.create(UserService.class));
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService());
    }
}

```

Потом этот resolver мы привязываем к классу и используем с помощью **@ExtendWith**

```
@ExtendWith({
        UserServiceParamResolver.class
})
public class UserServiceTest {
```

## 8. Method Order

```
//@TestMethodOrder(MethodOrderer.Random.class) тесты в рандомной порядке
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class) тесты вызываются в порядке, который мы задалив @Order(номер)
//@TestMethodOrder(MethodOrderer.MethodName.class) тесты вызываются в алфавитном порядке
//@TestMethodOrder(MethodOrderer.DisplayName.class) тесты вызываются в алфавитном порядке аннотаций, помеченных DisplayName
public class UserServiceTest {
```

- (MethodOrderer.Random.class) тесты в рандомной порядке
- (MethodOrderer.OrderAnnotation.class) тесты вызываются в порядке, который мы задали @Order(номер)
- (MethodOrderer.MethodName.class) тесты вызываются в алфавитном порядке
- (MethodOrderer.DisplayName.class) тесты вызываются в алфавитном порядке аннотаций, помеченных DisplayName. *Если DisplayName нет, то будет по умолчанию DisplayName = названию метода*
----

```
    @Test
//    @Order(0) для @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//    @DisplayName("name") для MethodOrderer.DisplayName.class
    void UsersEmptyIfNoAdded() {
        var users = userService.getAll();

        assertThat(users).hasSize(0);
        assertThat(users).isEmpty();
    }
```

**Лучше не использовать!**

## 9. Nested classes

Для того, чтобы разделить тесты на модули, их можно разбить по разным классам в основном классе. 
> **Nested class** - статический класс внутри класса
> **Inner class** - **не** статический класс внутри класса

**В тестах используются nested классы для такого модулирования, но вместо *модификатора static*, мы используем аннотацию *@Nested***
## 10. Parametrised tests

```
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
    <version>5.9.3</version>
    <scope>test</scope>
</dependency>
```

```
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
```

Редко используемые: **используются только с 1 аргументом**

- @ArgumentsSource() - мы должны передать сюда класс implements ArgumentsProvider
- @NullSource - подставляет null в параметр
- @EmptySource - подходит для массивов (в том числе для строк)
- @NullAndEmptySource
- @ValueSource(strings = {"name1", "name2"}) - по очереди вызовет фцию с name1 - 1 и с name2 - 2
- @EnumSource

### Самый популярный

- @MethodSource(“адрес на статический метод, который предоставляет нам аргументы“)

```
@MethodSource("com.rybina.service.UserServiceTest#getArgsForLoginTest")

--------

static Stream<Arguments> getArgsForLoginTest() {
    return Stream.of(
            Arguments.of(user1.getName(), user1.getPassword(), Optional.of(user1)),
            Arguments.of(user2.getName(), user2.getPassword(), Optional.of(user2)),
            Arguments.of(user2.getName(), "dummy", Optional.empty()),
            Arguments.of("dummy", user2.getPassword(), Optional.empty())
    );
}
```


### Тоже популярные но не могут передавать сложные типы
- @CsvFileSource(resources = "/login-test-data.csv", delimiter = ',', numLinesToSkip = 1)

![](Screenshot%202023-11-24%20at%2000.45.15.png)<!-- {"width":317} -->![](Screenshot%202023-11-24%20at%2000.45.29.png)<!-- {"width":224} -->

- @CsvSource
```
@CsvSource({
        "test1, test1",
        "test2, test2"
})
void loginParametrizedText(String name, String password) {
    userService.add(user1, user2);

    Optional<User> user = userService.login(name, password);
    assertThat(user).isPresent();
}
```

## 11. Flaky tests (нестабильные тесты)

> **Flaky tests** - нестабильные тесты, которые, например проходят через раз и тд.

Для того, чтобы избежать неточностей, подобные тесты надо вызывать по несколько раз за период тестирования. Для этого мы имеем аннотацию **@RepeatedTest**(name = <название каждой итерации для метода>, value = <кол-во итераций>)

Пример:

```
@RepeatedTest(name = RepeatedTest.LONG_DISPLAY_NAME, value = 5)
void loginSuccessIfUserExists() {
```

## 12. Timeouts
Для того, чтобы проверить длительность выполнения метода мы можем использовать:
1. assertTimeOut(<время>, () -> <наша фция>)

```
Optional<User> user = assertTimeout(Duration.ofMillis(200L), () -> userService.login(user1.getName(), user1.getPassword()));
```

2. *assertTimeoutPreemptively* - аналогичен *1*, но он работает в своем потоке
3. @Timeout(value = <время>, unit = <ед. изменения>)
```
@Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
class LoginTest {
```

```
@Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
@Test
void loginFailIfNameIncorrect() {
```

## 13. ExtensionModal![](Screenshot%202023-11-24%20at%2019.30.01.png)<!-- {"width":444} -->

— это этапы, которые мы можем расширить 

### 13. 1 Test lifecycle callbacks

![](Screenshot%202023-11-24%20at%2019.40.50.png)<!-- {"width":444} -->

Красное - это методы - колбеки, с помощью которых мы расширяем этапы нашего жизненного цикла

Пример:

```
public class GlobalExtention implements BeforeAllCallback, BeforeTestExecutionCallback {

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        System.out.println("Before All callback");
    }

    @Override
    public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
        System.out.println("Before Each callback");
    }
}

```

### 13. 2 Test instance post-processing (implements TestInstancePostProcessor)

> Данный класс работает тогда, когда объект нашего теста толко создался

```
public class PostProcessingExtension implements TestInstancePostProcessor {

    //метод вызывается тогда, когда объект теста только только создался
    //testInstance - объект нашего класса
    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        System.out.println("postprocessing");
        var declaredFields = testInstance.getClass().getDeclaredFields();
        for (Field declaredField : declaredFields) {
            if (declaredField.isAnnotationPresent(Getter.class)) {
                declaredField.set(testInstance, new UserService());
            }
        }
    }
}
```

### 13. 3 Conditional Test Execution (implements ExecutionCondition)
> Это класс, который вызывается перед запуском каждого теста для того, чтобы решить - *запускать нам вообще тест или нет*

```
public class ConditionalExtention implements ExecutionCondition {

    //    стоит ли нам вызывать этот тест??
    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
        return System.getProperty("skip") != null
                ? ConditionEvaluationResult.disabled("test is skipped")
                : ConditionEvaluationResult.enabled("enabled by default");
    }
}
```

### 13. 4 Parameter Resolver (implements ParameterResolver)

```
public class UserServiceParamResolver implements ParameterResolver {

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return parameterContext.getParameter().getType() == UserService.class ;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
//        кеширование
//        Namespace - ключ для стора. раскрываем как hashmap
        var store = extensionContext.getStore(ExtensionContext.Namespace.create(UserService.class));
        return store.getOrComputeIfAbsent(UserService.class, it -> new UserService());
    }
}
```

### 13. 5 Exception Handling

> Этот класс вызывается тогда, когда тест выбрасывает какой-то **Exception** и реагирует как-то на это

```
public class ThrowableException implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if(throwable instanceof IOException) {
            throw throwable;
        }
    }
}
```

 — в данном случае, мы никак не реагируем на выброс ошибки. Тут мы скорректировали выброс нашей ошибки так, чтобы при выбросе чего угодно ЧТО НЕ ЯВЛЯЕТСЯ *instanceof IOException* тест НЕ падал

## 14. Mockito

### 14. 1 Test Doubles

> **Dummy** - объекты, которые не используется во время тестирования. Нужны только для заполнения параметров методов

> **Fake** - объекты с работающим функционалом, но не подходящим для production. Например, запуск in-memory базы данных для тестирования DAO

> **Stub** - объекты, которые используется mocks и spies для ответа (Answer) на вызовы методов во время тестов

> **Mock** - запрограммированные объекты, возвращающие ожидаемый результат (stubs) на вызов определенных

> **Spy** - proxy для реальных объектов, которые ведут себя точно также, но могут быть запрограммированы как **mocks**

### 14. 2 Mocks

> **Mock** - это по сути класс, который наследуется от нашего класса и переписывает все его методы на пустышки, которые возвращают дефолтные значения

**Моки стоит инициализировать в BeforeEach чтобы потом не использовать Mock.reset**

### 14. 3 Stub

```
Mockito.doReturn(true).when(userDao).delete(user1.getId());
Mockito.when(userDao.delete(user1.getId())).thenReturn(true).thenReturn(false);


```

### 14. 4 Dummy

`Mockito.any()` - dummy объект

```
Mockito.doReturn(true).when(userDao).delete(Mockito.any());
```

### 14. 5 Spy

> **Spy** - тот же мок, но он хранит в себе объект оригинального нашего класса и если мы не настроили *answers* в наших *stubs*, он возвращает реальный метод

Пример реализации
```
public class UserDaoSpy extends UserDao {
    
    private final UserDao userDao;
    private Map<Integer, Boolean> answers = new HashMap<>();

    public UserDaoSpy(UserDao userDao) {
        this.userDao = userDao;
    }

    @Override
    public boolean delete(Integer userId) {
        return answers.getOrDefault(userId, userDao.delete(userId));
    }
}
```

### 14. 6 Verify

— Проверяет взаимодействие с нашим моком: сколько раз мы его вызывали

```
Mockito.verify(userDao, Mockito.times(2)).delete(user1.getId());
Mockito.verify(userDao, Mockito.atLeast(2)).delete(user1.getId());
Mockito.verifyNoInteractions(userDao); - проверка не было ли взаимодействия с данным моком
```

### 14. 6. + ArgumentCaptor

```
boolean deleteResult = userService.delete(user1.getId());

ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

Mockito.verify(userDao).delete(argumentCaptor.capture());

assertThat(argumentCaptor.getValue()).isEqualTo(user1.getId()); -- true
assertThat(argumentCaptor.getValue()).isEqualTo(25); -- false
```

Сздаение ArgumentCaptor:
`ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);`

**ArgumentCaptor** - перехватывает аргументы во время вызова метода нашего мока. Перехватить эти аргументы мы можем в **verify**, 
- argumentCaptor.getValue() - возвращает последний принятый аргумент
- argumentCaptor.getValues() - возвращает список всех аргументов

## 15. Mockito extension 
**Важно при использовании аннотаций, моки, которые лежат внутри класса с @InjectMocks НЕ МОГУТ БЫТЬ final ПОЛЕМ!!**

> **Mockito extension** - extension как и в 13 главе, которое позволяет библиотеке Mockito контролировать Моки на протяжении всего жизненного цикла нашего теста

Благодаря этому, мы можем использовать аннотации @Mock, @InjectMocks, @Spy, @Captor (ArgumentCaptor)

## 16. BDD (Behaviour Driven Development)

> **BDD** - способ тестирования, когда мы составляем сценарии, которые тестируем. **Больше подходит для acceptance test**
> **Story** - наше общее тестирование, которое состоит из **scenarios**
> **Scenario** - один из случаев применения нашего приложение. Сценарий состоит из: 
> - given
> - when
> - then

По сути, наш тест можно представить так:
```
@Test
void shouldDeleteExistingUser() {

----- GIVEN
    userService.add(user1);
    Mockito.doReturn(true).when(userDao).delete(user1.getId());

----- WHEN
    boolean deleteResult = userService.delete(user1.getId());

----- THEN
    Mockito.verify(userDao).delete(argumentCaptor.capture());

    assertThat(argumentCaptor.getValue()).isEqualTo(user1.getId());
    assertThat(deleteResult).isTrue();
}
```

Аналог с BDD mockito

```
    @Test
    void shouldDeleteExistingUser() {
        userService.add(user1);
//        doReturn(true).when(userDao).delete(user1.getId());

---
        BDDMockito.given(userDao.delete(user1.getId())).willReturn(true);

		ИЛИ

        BDDMockito.willReturn(true).given(userDao).delete(user1.getId());
---
        boolean deleteResult = userService.delete(user1.getId());

        Mockito.verify(userDao).delete(argumentCaptor.capture());

        assertThat(argumentCaptor.getValue()).isEqualTo(user1.getId());
        assertThat(deleteResult).isTrue();
    }
```
