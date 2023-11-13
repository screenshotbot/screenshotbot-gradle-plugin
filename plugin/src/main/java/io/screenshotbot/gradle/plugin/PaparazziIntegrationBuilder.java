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

                        if (task.getName().startsWith("recordPaparazzi")) {
                            prepareTask(task, tasks, project, "record");
                            prepareTask(task, tasks, project, "verify");
                            prepareTask(task, tasks, project, "ci");
                        }

                    });
                });
            }
        };
        project.getPlugins().withId("app.cash.paparazzi", action);

    }


    private void prepareTask(Task task, TaskContainer tasks, Project project, String mode) {
        String _taskName = task.getName();
        String nameWithoutPrefix = task.getName().substring("record".length());
        if (mode.equals("verify")) {
            _taskName = "verify" + nameWithoutPrefix + "Screenshotbot";
        } else if (mode.equals("ci")) {
            _taskName = "recordAndVerify" + nameWithoutPrefix + "ScreenshotbotCI";
        } else {
            _taskName += "Screenshotbot"; // For record
        }

        String taskName = _taskName;
        String backupSnapshots = taskName + "BackupSnapshots";
        String restoreSnapshots = taskName + "RestoreSnapshots";
        String uploadSnapshots = taskName + "UploadSnapshots";

        task.mustRunAfter(backupSnapshots);
        tasks.register(taskName,
                        RecordPaparazziTask.class)
                .configure((it) -> {
                    it.setGroup("Screenshotbot");
                    it.setDescription("Records paparazzi screenshots into Screenshotbot");
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
                    it.directory = new File(getSnapshotsDir(project).getAsFile(), "images");
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


    @NotNull
    private static Directory getSnapshotsDir(Project project) {
        return project.getLayout().getProjectDirectory().dir("src/test/snapshots");
    }

}
