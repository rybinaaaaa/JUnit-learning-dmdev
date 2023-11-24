package com.rybina;

import com.rybina.extentions.*;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@TestMethodOrder(MethodOrderer.Random.class)
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class) тесты вызываются в порядке, который мы задалив @Order(номер)
//@TestMethodOrder(MethodOrderer.MethodName.class) тесты вызываются в алфавитном порядке
//@TestMethodOrder(MethodOrderer.DisplayName.class) тесты вызываются в алфавитном порядке аннотаций, помеченных DisplayName
@ExtendWith({
        UserServiceParamResolver.class,
        GlobalExtention.class,
        PostProcessingExtension.class,
        ConditionalExtention.class,
//        ThrowableException.class
})
public class TestBase {
}
