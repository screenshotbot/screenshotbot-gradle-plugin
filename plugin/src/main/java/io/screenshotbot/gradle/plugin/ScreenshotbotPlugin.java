package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;

import java.util.ArrayList;
import java.util.List;

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

        public String getMainBranch() {
            return mainBranch;
        }

        public void setMainBranch(String mainBranch) {
            this.mainBranch = mainBranch;
        }

        private List<String> extraArgs = new ArrayList<>();

        /**
         * Additional arguments to pass to the Screenshotbot CLI tool.
         *
         * @param extraArgs
         */
        public void setExtraArgs(List<String> extraArgs) {
            this.extraArgs = extraArgs;
        }

        public List<String> getExtraArgs() {
            return this.extraArgs;
        }

        private String mainBranch;

        private String repoUrl;


        public String getRepoUrl() {
            if (repoUrl == null) {
                for (int i = 0; i < getExtraArgs().size() - 1; i++) {
                    if (getExtraArgs().get(i).equals("--repo-url")) {
                        return getExtraArgs().get(i+1);
                    }
                }
            }
            return repoUrl;
        }

        public void setRepoUrl(String repoUrl) {
            this.repoUrl = repoUrl;
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


        String uploadCommitGraphOnScreenshotbot = "uploadCommitGraphOnScreenshotbot_" +
                String.valueOf(extension.getMainBranch());
        if (target.getRootProject().getTasks().findByName(uploadCommitGraphOnScreenshotbot) == null) {
            target.getRootProject().getTasks().register(uploadCommitGraphOnScreenshotbot, UploadCommitGraphTask.class)
                    .configure((it) -> {
                        // The CLI tool needs the main branch in order to
                        // fetch commits from the origin.
                        it.setMainBranch(extension.getMainBranch());
                        it.dependsOn(":downloadScreenshotbotRecorder");
                    });
        }

        target.getTasks().register("installScreenshotbot", InstallScreenshotbotTask.class)
                .configure((it) -> {
                   it.setGroup("Screenshotbot");
                   it.setDescription("Install Screenshotbot credentials interactively");
                   it.hostname = extension.getHostname();
                   it.dependsOn(":downloadScreenshotbotRecorder");
                });


        registerRootTask(target, "downloadScreenshotbotRecorder", DownloadRecorderTask.class);
    }

    private static void registerRootTask(Project target, String taskName, Class<? extends Task> taskClass) {
        if (target.getRootProject().getTasks().findByName(taskName) == null) {
            target.getRootProject().getTasks().register(taskName, taskClass);
        }
    }
}
