package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

public class ShotIntegrationBuilder extends AbstractIntegrationBuilder{

    public static final String SUFFIX = "ExecuteScreenshotTests";

    @Override
    protected @NotNull String getPluginId() {
        return "shot";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        return task.getName().endsWith(SUFFIX) && task.getGroup().toLowerCase().equals("shot");
    }

    @Override
    protected @NotNull String getPluginName() {
        return "Shot";
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        String flavor = upcaseFirst(getFlavor(task));
        if (mode.equals("ci")) {
            return "recordAndVerify" + flavor + "ScreenshotbotCI";
        } else {
            return mode + flavor + "Screenshotbot";
        }
    }


    @Override
    protected @NotNull Directory getSnapshotsDir(Project project, Task task) {
        String flavor = getFlavor(task);
        // todo: fix camelcase to be directory structure here instead
        return project.getLayout().getProjectDirectory().dir("screenshots").dir(flavor);
    }

    @NotNull
    private static String getFlavor(Task task) {
        return task.getName().substring(0, task.getName().length() - SUFFIX.length());
    }
}
