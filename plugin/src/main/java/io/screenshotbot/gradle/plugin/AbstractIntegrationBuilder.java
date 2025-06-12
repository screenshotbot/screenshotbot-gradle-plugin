package io.screenshotbot.gradle.plugin;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.TaskContainer;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public abstract class AbstractIntegrationBuilder {
    private final ScreenshotbotPlugin.Extension extension;

    public AbstractIntegrationBuilder(ScreenshotbotPlugin.Extension extension) {
        this.extension = extension;
    }

    protected void safeDelete(File file) {
        if (!file.delete()) {
            throw new RuntimeException("Could not delete: " + file);
        }
    }

    protected void restoreDir(Directory snapshotsDir) {
        String backupDir = snapshotsDir.getAsFile().toString() + "-screenshotbot-backup";

        if (snapshotsDir.getAsFile().exists()) {
            deleteDirectory(snapshotsDir.getAsFile());
        }

        if (new File(backupDir).exists()) {
            try {
                Files.move(Path.of(backupDir), snapshotsDir.getAsFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


    private void deleteDirectory(File asFile) {
        for (File file : asFile.listFiles()) {
            if (file.isDirectory() && !Files.isSymbolicLink(file.toPath())) {
                deleteDirectory(file);
            } else {
                safeDelete(file);
            }
        }
        safeDelete(asFile);
    }

    protected void backupDir(Directory dir) {
        try {
            String dest = dir.getAsFile().toString() + "-screenshotbot-backup";
            // This isn't ideal.. but if the directory already exists, let's assume it was a previous backup, so we should restore it.
            if (!new File(dest).exists() && dir.getAsFile().exists()) {
                Path src = dir.getAsFile().toPath();
                Files.move(src,
                        Path.of(dest), ATOMIC_MOVE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected void prepareTask(Task inputTask, Project project, String mode) {
        TaskContainer tasks = project.getTasks();

        String taskName = generateTaskName(inputTask, mode);
        String backupSnapshots = taskName + "BackupSnapshots";
        String restoreSnapshots = taskName + "RestoreSnapshots";
        String uploadSnapshots = taskName + "UploadSnapshots";
        List<Directory> snapshotsDirList = getSnapshotsDirList(project, inputTask);
        List<File> imageDirList = new ArrayList<>();
        for (Directory snapshotDir : snapshotsDirList) {
            imageDirList.add(getImagesDirectory(snapshotDir.getAsFile()));
        }

        String inputTaskName = inputTask.getName();
        String channelName = project.getPath();

        inputTask.mustRunAfter(backupSnapshots);
        tasks.register(taskName,
                        RecordPaparazziTask.class)
                .configure((it) -> {
                    it.setGroup("Screenshotbot");
                    it.setDescription("Records " + getPluginName() + " screenshots into Screenshotbot");
                    it.dependsOn(inputTaskName);
                    it.dependsOn(backupSnapshots);
                    it.dependsOn(uploadSnapshots);
                    it.dependsOn(restoreSnapshots);

                    configureTaskDependencies(it, inputTaskName);
                });

        tasks.register(backupSnapshots).configure((it) -> {
            configureBackupSnapshotsDependencies(it, inputTaskName);
            it.doFirst((it2) -> {
                for (var dir : snapshotsDirList) {
                    backupDir(dir);
                }
            });
        });



        tasks.register(uploadSnapshots, UploadScreenshotsTask.class)
                .configure((it) -> {
                    it.possibleDirectories = imageDirList;
                    it.channel = extension.getChannelPrefix() + channelName;
                    it.mode = mode;
                    it.hostname = extension.getHostname();
                    it.batch = extension.getBatch();
                    it.mainBranch = extension.getMainBranch();
                    it.extraArgs = extension.getExtraArgs();
                    it.repoUrl = extension.getRepoUrl();
                    it.mustRunAfter(inputTaskName);
                    it.dependsOn(":downloadScreenshotbotRecorder");
                    it.doFirst((innerTask) -> {
                    });
                });

        tasks.register(restoreSnapshots)
                .configure((it) -> {
                    it.mustRunAfter(uploadSnapshots);
                    it.doFirst((innerTask) -> {
                        for (var dir : snapshotsDirList) {
                            restoreDir(dir);
                        }
                    });
                });
    }

    protected void configureBackupSnapshotsDependencies(Task it, String taskName) {
    }

    protected void configureTaskDependencies(RecordPaparazziTask it, String sourceTask) {
    }


    public void apply(Project project) {
        Action<Plugin> action = new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                project.afterEvaluate((project) -> {
                    TaskContainer tasks = project.getTasks();

                    // Avoid a ConcurrentModificationException
                    var filtered = tasks.stream().filter((it) -> isApplicableTask(it)).toList();

                    filtered.stream().forEach((task) -> {
                        prepareTask(task, project, "record");
                        prepareTask(task, project, "verify");
                        prepareTask(task, project, "ci");
                    });
                });
            }
        };

        project.getPlugins().withId(getPluginId(), action);

    }

    @NotNull
    protected abstract String getPluginId();

    protected abstract boolean isApplicableTask(Task task);

    /*
     * This might be the same as getSnapshotsDir(), but for example with Paparazzi the
     * snapshots dir is the directory we're backing up, but the images directory is snapshots-dir/images.
     */
    @NotNull
    public File getImagesDirectory(File snapshotsDir) {
        return snapshotsDir;
    }

    @NotNull
    protected abstract String getPluginName();

    @NotNull
    protected abstract String generateTaskName(Task task, String mode);

    @NotNull
    protected abstract Directory getSnapshotsDir(Project project, Task task);

    protected List<Directory> getSnapshotsDirList(Project project, Task task) {
        return List.of(getSnapshotsDir(project, task));
    }

    public String upcaseFirst(String str) {
        if (str.equals("")) {
            return str;
        } else {
            return str.substring(0,1).toUpperCase() + str.substring(1);
        }
    }

    public String downcaseFirst(String str) {
        if (str.equals("")) {
            return str;
        } else {
            return str.substring(0,1).toLowerCase() + str.substring(1);
        }
    }
}
