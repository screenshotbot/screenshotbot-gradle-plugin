package io.screenshotbot.gradle.plugin;

import groovy.lang.Closure;
import org.gradle.api.*;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class PaparazziIntegrationBuilder {
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

    private void restoreDir(Directory snapshotsDir) {
        String backupDir = snapshotsDir.getAsFile().toString() + "-screenshotbot-backup";

        deleteDirectory(snapshotsDir.getAsFile());

        if (new File(backupDir).exists()) {
            try {
                Files.move(Path.of(backupDir), snapshotsDir.getAsFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void safeDelete(File file) {
        if (!file.delete()) {
            throw new RuntimeException("Could not delete: " + file);
        }
    }
    private void deleteDirectory(File asFile) {
        for (File file : asFile.listFiles()) {
            // I want to avoid having bugs here that accidently delete outside of this directory, and
            // I don't want dependencies, so I'll hardcode some of the directories I'm allowed to recurse
            // into
            if (file.isDirectory() && file.getName().equals("images")) {
                deleteDirectory(file);
            } else {
                safeDelete(file);
            }
        }
        safeDelete(asFile);
    }


    @NotNull
    private static Directory getSnapshotsDir(Project project) {
        return project.getLayout().getProjectDirectory().dir("src/test/snapshots");
    }

    private void backupDir(Directory dir) {
        try {
            String dest = dir.getAsFile().toString() + "-screenshotbot-backup";
            // This isn't ideal.. but if the directory already exists, let's assume it was a previous backup, so we should restore it.
            if (!new File(dest).exists()) {
                Files.move(dir.getAsFile().toPath(),
                        Path.of(dest), ATOMIC_MOVE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}