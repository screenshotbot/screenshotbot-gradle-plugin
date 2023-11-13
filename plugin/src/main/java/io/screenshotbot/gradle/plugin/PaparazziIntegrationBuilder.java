package io.screenshotbot.gradle.plugin;

import org.gradle.api.*;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;

public class PaparazziIntegrationBuilder extends AbstractIntegrationBuilder {
    public void apply(Project project) {
        Action<Plugin> action = new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                project.afterEvaluate((project) -> {
                    TaskContainer tasks = project.getTasks();
                    tasks.stream().forEach((task) -> {

                        if (isApplicableTask(task)) {
                            prepareTask(task, project, "record");
                            prepareTask(task, project, "verify");
                            prepareTask(task, project, "ci");
                        }

                    });
                });
            }
        };
        project.getPlugins().withId(getPluginId(), action);

    }

    @NotNull
    private String getPluginId() {
        return "app.cash.paparazzi";
    }

    private boolean isApplicableTask(Task task) {
        return task.getName().startsWith("recordPaparazzi");
    }


    private void prepareTask(Task task,  Project project, String mode) {
        TaskContainer tasks = project.getTasks();

        String taskName = generateTaskName(task, mode);
        String backupSnapshots = taskName + "BackupSnapshots";
        String restoreSnapshots = taskName + "RestoreSnapshots";
        String uploadSnapshots = taskName + "UploadSnapshots";

        task.mustRunAfter(backupSnapshots);
        tasks.register(taskName,
                        RecordPaparazziTask.class)
                .configure((it) -> {
                    it.setGroup("Screenshotbot");
                    it.setDescription("Records " + getPluginName() + " screenshots into Screenshotbot");
                    it.dependsOn(task.getName());
                    it.dependsOn(backupSnapshots);
                    it.dependsOn(uploadSnapshots);
                    it.dependsOn(restoreSnapshots);

                });
        tasks.register(backupSnapshots).configure((it) -> {
            it.doFirst((it2) -> {
                backupDir(getSnapshotsDir(project));
            });
        });

        tasks.register(uploadSnapshots, UploadScreenshotsTask.class)
                .configure((it) -> {
                    it.directory = getImagesDirectory(project);
                    it.channel = project.getPath();
                    it.mode = mode;

                    it.mustRunAfter(task.getName());
                    it.doFirst((innerTask) -> {

                    });
                });

        tasks.register(restoreSnapshots)
                .configure((it) -> {
                    it.mustRunAfter(uploadSnapshots);
                    it.doFirst((innerTask) -> {
                        restoreDir(getSnapshotsDir(project));
                    });
                });
    }

    /*
     * This might be the same as getSnapshotsDir(), but for example with Paparazzi the 
     * snapshots dir is the directory we're backing up, but the images directory is snapshots-dir/images. 
     */
    @NotNull
    @Override
    protected File getImagesDirectory(Project project) {
        return new File(getSnapshotsDir(project).getAsFile(), "images");
    }

    @NotNull
    @Override
    protected String getPluginName() {
        return "Paparazzi";
    }

    @NotNull
    @Override
    protected String generateTaskName(Task task, String mode) {
        String taskName = task.getName();
        String nameWithoutPrefix = task.getName().substring("record".length());
        if (mode.equals("verify")) {
            taskName = "verify" + nameWithoutPrefix + "Screenshotbot";
        } else if (mode.equals("ci")) {
            taskName = "recordAndVerify" + nameWithoutPrefix + "ScreenshotbotCI";
        } else {
            taskName += "Screenshotbot"; // For record
        }

        return taskName;
    }


    @NotNull
    @Override
    protected Directory getSnapshotsDir(Project project) {
        return project.getLayout().getProjectDirectory().dir("src/test/snapshots");
    }

}
