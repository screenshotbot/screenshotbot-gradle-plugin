package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class ShotIntegrationBuilder extends AbstractIntegrationBuilder{

    public static final String SUFFIX = "DownloadScreenshots";

    public ShotIntegrationBuilder(ScreenshotbotPlugin.Extension extension) {
        super(extension);
    }

    @Override
    protected void configureBackupSnapshotsDependencies(Task it, String taskName) {
        it.mustRunAfter(getInstrumentationTaskName(getFlavorFromTaskName(taskName)));
    }


    @Override
    protected void configureTaskDependencies(RecordPaparazziTask it, String sourceTask) {
        String executeTask = getInstrumentationTaskName(getFlavorFromTaskName(sourceTask));
        it.dependsOn(executeTask);

    }

    private String getInstrumentationTaskName(String flavor) {
        return "connected" + upcaseFirst(flavor) + "AndroidTest";
    }

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
    public File getImagesDirectory(Project project, Task task) {
        return getSnapshotsDir(project, task).dir("screenshots-default").getAsFile();
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project, Task task) {
        String flavor = getFlavor(task);
        // todo: fix camelcase to be directory structure here instead
        return project.getLayout().getProjectDirectory().dir("screenshots").dir(flavor);
    }

    @NotNull
    private static String getFlavor(Task task) {
        String taskName = task.getName();
        return getFlavorFromTaskName(taskName);
    }

    @NotNull
    private static String getFlavorFromTaskName(String taskName) {
        return taskName.substring(0, taskName.length() - SUFFIX.length());
    }
}
