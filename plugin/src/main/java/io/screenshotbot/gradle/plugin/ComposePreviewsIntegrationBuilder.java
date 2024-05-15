package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

public class ComposePreviewsIntegrationBuilder extends AbstractIntegrationBuilder {
    public ComposePreviewsIntegrationBuilder(ScreenshotbotPlugin.Extension extension) {
        super(extension);
    }

    @Override
    protected @NotNull String getPluginId() {
        return "com.android.compose.screenshot";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        return false;
    }

    @Override
    protected @NotNull String getPluginName() {
        return null;
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        return null;
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project, Task task) {
        return null;
    }
}
