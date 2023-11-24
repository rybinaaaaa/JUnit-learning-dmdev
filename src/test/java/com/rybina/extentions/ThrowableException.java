package com.rybina.extentions;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;

import java.io.IOException;

public class ThrowableException implements TestExecutionExceptionHandler {

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable {
        if(throwable instanceof IOException) {
            throw throwable;
        }
    }
}
