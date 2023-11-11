package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;

public class ScreenshotbotPlugin implements Plugin<Project> {
    public static class Extension {
        String hostname = "https://api.screenshotbot.io";
    }
    @Override
    public void apply(Project target) {
        target.getExtensions().create("screenshotbot", Extension.class);
    }
}
