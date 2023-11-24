package com.rybina.extentions;

import com.rybina.service.UserService;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

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
