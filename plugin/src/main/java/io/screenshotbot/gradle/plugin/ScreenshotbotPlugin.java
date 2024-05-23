package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;

import java.awt.*;

public class ScreenshotbotPlugin implements Plugin<Project> {
    public static class Extension {

        private String hostname = "https://api.screenshotbot.io";
        
        private String batch = null;

        public String getChannelPrefix() {
            return channelPrefix;
        }

        /*
         * By default we use the Gradle module name to determine the channel
         * name in Screenshotbot. But if you have multiple Gradle projects, this
         * might lead to a name collision. Using a channelPrefix
         */
        public void setChannelPrefix(String channelPrefix) {
            this.channelPrefix = channelPrefix;
        }

        private String channelPrefix = "";

        public String getHostname() {
            return hostname;
        }

        /*
         * The API endpoint to use for Screenshotbot.
         *
         * This must be changed when using the Open-source or Enterprise
         * versions of screenshotbot.
         */
        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
        public String getBatch() {
            return batch;
        }

        /*
         * "Batch" all screenshots from all Gradle modules under a
         * single GitHub check result. (Or GitLab, BitBucket build
         * status etc.)
         *
         * This won't affect local runs.
         */
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
        new ComposePreviewsIntegrationBuilder(extension).apply(target);

        target.getTasks().register("installScreenshotbot", InstallScreenshotbotTask.class)
                .configure((it) -> {
                   it.setGroup("Screenshotbot");
                   it.setDescription("Install Screenshotbot credentials interactively");
                   it.hostname = extension.getHostname();
                });


        if (target.getRootProject().getTasks().findByName("downloadScreenshotbotRecorder") == null) {
            target.getRootProject().getTasks().register("downloadScreenshotbotRecorder", DownloadRecorderTask.class);
        }
    }
}
