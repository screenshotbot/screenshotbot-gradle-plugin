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
            List<String> copy = new ArrayList<>();
            copy.addAll(extraArgs);
            int pos = getRepoUrlPosInExtraArgs();
            if (pos >= 0) {
                copy.remove(pos);
                copy.remove(pos);
            }
            return copy;
        }

        private String mainBranch;

        private String repoUrl;


        public String getRepoUrl() {
            if (repoUrl == null) {
                int x = getRepoUrlPosInExtraArgs();
                if (x >= 0) {
                    return extraArgs.get(x + 1);
                }
            }
            return repoUrl;
        }

        private int getRepoUrlPosInExtraArgs() {
            int x = -1;
            for (int i = 0; i < extraArgs.size() - 1; i++) {
                if (extraArgs.get(i).equals("--repo-url")) {
                    x = i;
                }
            }
            return x;
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
