package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

public class ComposePreviewsIntegrationBuilder extends AbstractIntegrationBuilder {

    public static final String PREFIX = "update";
    public static final String SUFFIX = "ScreenshotTest";

    public ComposePreviewsIntegrationBuilder(ScreenshotbotPlugin.Extension extension) {
        super(extension);
    }

    @Override
    protected @NotNull String getPluginId() {
        return "com.android.compose.screenshot";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        return task.getName().startsWith(PREFIX) && task.getName().endsWith(SUFFIX);
    }

    @Override
    protected @NotNull String getPluginName() {
        return "ComposePreviews";
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        String suffix = task.getName().substring(PREFIX.length());
        if (mode.equals("ci")) {
            return "recordAndVerify" + suffix;
        } else if (mode.equals("record")) {
            return "record" + suffix;
        } else if (mode.equals("verify")) {
            return "verify" + suffix;
        } else {
            throw new RuntimeException("unknown mode: " + mode);
        }
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project, Task task) {
        return project.getLayout().getProjectDirectory().dir("src/"  + getVariant(task) + "/screenshotTest/reference");
    }

    private String getVariant(Task task) {
        return downcaseFirst(task.getName().substring(PREFIX.length(), task.getName().length() - SUFFIX.length()));
    }
}
