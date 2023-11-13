package io.screenshotbot.gradle.plugin;

import groovy.lang.Closure;
import org.gradle.api.*;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ScreenshotbotPlugin implements Plugin<Project> {
    public static class Extension {
        String hostname = "https://api.screenshotbot.io";
    }
    @Override
    public void apply(Project target) {
        target.getExtensions().create("screenshotbot", Extension.class);

        new PaparazziIntegrationBuilder().apply(target);
        new FacebookIntegrationBuilder().apply(target);
    }
}
