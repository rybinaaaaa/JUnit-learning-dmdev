package com.rybina;

import org.junit.jupiter.api.TestInfo;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;

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
//                .listeners()
                .filters(
                        TagFilter.excludeTags("login")
                )
                .build();
        launcher.execute(request, summaryGeneratedListener);

        try(var writer = new PrintWriter(System.out)) {
            summaryGeneratedListener.getSummary().printTo(writer);
        }
    }
}
