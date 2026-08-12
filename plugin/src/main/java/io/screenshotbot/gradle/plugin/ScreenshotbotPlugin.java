package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;
import org.gradle.api.configuration.BuildFeatures;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class ScreenshotbotPlugin implements Plugin<Project> {

    @Inject
    protected abstract BuildFeatures getBuildFeatures();

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

    /*
     * There's a single :downloadScreenshotbotRecorder shared by every module, so
     * it belongs on the root project.
     *
     * Isolated Projects forbids touching another project's task container, so
     * when it's active we can only register the task if we're being applied to
     * the root project ourselves. That's why users with Isolated Projects have
     * to apply this plugin to their root project too. (See GitHub #5.)
     *
     * Depending on :downloadScreenshotbotRecorder from another project is still
     * allowed under Isolated Projects, so the tasks that use the recorder don't
     * need to know which of these two paths we took.
     */
    private void registerRootTask(Project target, String taskName, Class<? extends Task> taskClass) {
        if (isIsolatedProjects()) {
            if (!Project.PATH_SEPARATOR.equals(target.getPath())) {
                // The root project's copy of the plugin registers this for us.
                return;
            }
            registerIfAbsent(target, taskName, taskClass);
        } else {
            registerIfAbsent(target.getRootProject(), taskName, taskClass);
        }
    }

    private boolean isIsolatedProjects() {
        return getBuildFeatures().getIsolatedProjects().getActive().getOrElse(false);
    }

    private static void registerIfAbsent(Project host, String taskName, Class<? extends Task> taskClass) {
        if (host.getTasks().findByName(taskName) == null) {
            host.getTasks().register(taskName, taskClass);
        }
    }
}
