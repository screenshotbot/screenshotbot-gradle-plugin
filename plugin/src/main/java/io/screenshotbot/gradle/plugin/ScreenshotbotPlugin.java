package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;

public class ScreenshotbotPlugin implements Plugin<Project> {
    public static class Extension {
        private String hostname = "https://api.screenshotbot.io";
        private String batch = null;

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
        public String getBatch() {
            return batch;
        }

        public void setBatch(String batch) {
            this.batch = batch;
        }
    }
    @Override
    public void apply(Project target) {
        Extension extension = target.getExtensions().create("screenshotbot", Extension.class);

        new PaparazziIntegrationBuilder(extension).apply(target);
        new FacebookIntegrationBuilder(extension).apply(target);
        new ShotIntegrationBuilder(extension).apply(target);
        new RoborazziIntegrationBuilder(extension).apply(target);
        new DropshotsIntegrationBuilder(extension).apply(target);

        target.getTasks().register("installScreenshotbot", InstallScreenshotbotTask.class)
                .configure((it) -> {
                   it.setGroup("Screenshotbot");
                   it.setDescription("Install Screenshotbot credentials interactively");
                   it.hostname = extension.getHostname();
                });
    }
}
