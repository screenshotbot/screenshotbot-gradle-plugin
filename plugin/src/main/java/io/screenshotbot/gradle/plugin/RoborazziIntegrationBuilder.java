package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;

public class RoborazziIntegrationBuilder extends AbstractIntegrationBuilder {

    public static final String PREFIX = "recordRoborazzi";

    public RoborazziIntegrationBuilder(ScreenshotbotPlugin.Extension extension) {
        super(extension);
    }

    @Override
    protected @NotNull String getPluginId() {
        return "io.github.takahirom.roborazzi";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        return task.getName().startsWith(PREFIX);
    }

    @Override
    protected @NotNull String getPluginName() {
        return "Roborazzi";
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        if (mode.equals("ci")) {
            return task.getName() + "ScreenshotbotCI";
        } else {
            return mode + task.getName().substring("record".length()) + "Screenshotbot";
        }
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project, Task task) {
        Object ext = project.getExtensions().getByName("roborazzi");
        try {
            Object result = ext.getClass().getMethod("getOutputDir").invoke(ext);
            DirectoryProperty prop = (DirectoryProperty) result;
            DirectoryProperty outputDir = prop.convention(project.getLayout().getBuildDirectory().dir("outputs/roborazzi"));
            return outputDir.get();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
