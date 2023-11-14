package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

public class ShotIntegrationBuilder extends AbstractIntegrationBuilder{
    @Override
    protected @NotNull String getPluginId() {
        return "shot";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        return task.getName().equals("executeScreenshotTask");
    }

    @Override
    protected @NotNull String getPluginName() {
        return "Shot";
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        if (mode.equals("ci")) {
            return "recordAndVerifyScreenshotbotCI"
        } else {
            return mode + "Screenshotbot";
        }
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project) {
        return null;
    }
}
