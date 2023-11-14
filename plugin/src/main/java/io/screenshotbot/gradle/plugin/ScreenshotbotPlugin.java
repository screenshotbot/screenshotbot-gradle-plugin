package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;

public class ScreenshotbotPlugin implements Plugin<Project> {
    public static class Extension {
        private String hostname = "https://api.screenshotbot.io";

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }
    @Override
    public void apply(Project target) {
        Extension extension = target.getExtensions().create("screenshotbot", Extension.class);

        new PaparazziIntegrationBuilder(extension).apply(target);
        new FacebookIntegrationBuilder(extension).apply(target);

        target.getTasks().register("installScreenshotbot", InstallScreenshotbotTask.class)
                .configure((it) -> {
                   it.setGroup("Screenshotbot");
                   it.hostname = extension.getHostname();
                });
    }
}
