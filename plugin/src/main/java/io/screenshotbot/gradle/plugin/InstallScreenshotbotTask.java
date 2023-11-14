package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class InstallScreenshotbotTask extends BaseRecorderTask {
    @TaskAction
    public void installScreenshotbot() {
        ensureLibraryInstalled();
        getProject().exec((it) -> {
            it.setExecutable(getExecutable());
            @NotNull ArrayList<String> args = prepareArgs();
            args.add("dev");
            args.add("install");
            it.setArgs(args);
            it.setStandardInput(System.in);
        });
    }
}
