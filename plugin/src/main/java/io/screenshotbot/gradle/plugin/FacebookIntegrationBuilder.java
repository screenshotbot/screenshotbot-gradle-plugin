package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

public class FacebookIntegrationBuilder extends AbstractIntegrationBuilder {

    public static final String SUFFIX = "AndroidTestScreenshotTest";
    public static final String PREFIX = "record";

    @Override
    protected @NotNull String getPluginId() {
        return "com.facebook.testing.screenshot";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        String name = task.getName();
        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
    }

    @Override
    protected @NotNull String getPluginName() {
        return "Facebook screenshot-tests-for-android";
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        String name = task.getName();
        String mid = name.substring(PREFIX.length(), name.length () - SUFFIX.length());

        String suffix = mid + "Screenshotbot";
        if (mode.equals("record")) {
            return "record" + suffix;
        } else if (mode.equals("verify")) {
            return "verify" + suffix;
        } else if (mode.equals("ci")) {
            return "recordAndVerify" + suffix + "CI";
        } else {
            throw new RuntimeException("Unknown mode: " + mode);
        }
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project) {
        Object ext = project.getExtensions().findByName("screenshots");
        assert(ext != null);
        String recordDir = null;
        try {
            try {
                recordDir = (String) ext.getClass().getMethod("getRecordDir").invoke(ext);
            } catch (InvocationTargetException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return project.getLayout().getProjectDirectory().dir(recordDir);
    }

    public File getImagesDirectory(Project project) {
        Directory dir = getSnapshotsDir(project);
        for (File file : dir.getAsFile().listFiles()) {
            if (file.isDirectory()) {
                return file;
            }
        }
        throw new RuntimeException("No image directories found");
    }
}
