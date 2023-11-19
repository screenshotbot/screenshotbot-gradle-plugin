package io.screenshotbot.gradle.plugin;

import org.gradle.StartParameter;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.file.Directory;
import org.gradle.api.tasks.GradleBuild;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DropshotsIntegrationBuilder extends AbstractIntegrationBuilder {

    public static final String PREFIX = "update";
    public static final String SUFFIX = "AndroidTestScreenshots";

    public DropshotsIntegrationBuilder(ScreenshotbotPlugin.Extension extension) {
        super(extension);
    }

    @Override
    protected void prepareTask(Task task, Project project, String mode) {

        String runRecord = getTaskThatRunsTests(task.getName());
        String path = project.getPath();

        if (mode.equals("record")) { // we don't want to call this thrice.
            project.getTasks().register(runRecord, GradleBuild.class).configure((it) -> {
                List<String> taskList = new ArrayList<>();
                taskList.add(path + task.getName());
                it.setTasks(taskList);
                StartParameter startParameter = new StartParameter();
                Map<String, String> args = new HashMap<>();
                args.put("dropshots.record", "true");
                startParameter.setSystemPropertiesArgs(args);
                it.setStartParameter(startParameter);
            });
        }

        super.prepareTask(task, project, mode);
    }

    @Override
    String getTaskThatRunsTests(String inputTaskName) {
        return inputTaskName + "ScreenshotbotHelper";
    }


    @Override
    protected @NotNull String getPluginId() {
        return "com.dropbox.dropshots";
    }

    @Override
    protected boolean isApplicableTask(Task task) {
        String name = task.getName();
        return name.startsWith(PREFIX) && name.endsWith(SUFFIX);
    }

    @Override
    protected @NotNull String getPluginName() {
        return "Dropshots";
    }

    @Override
    protected @NotNull String generateTaskName(Task task, String mode) {
        String flavor = upcaseFirst(task.getName().substring(PREFIX.length(), task.getName().length() - SUFFIX.length()));
        if (mode.equals("verify")) {
            return "update" + flavor + "ScreenshotbotCI";
        } else if (mode.equals("record")) {
            return "update" + flavor + "Screenshotbot";
        } else {
            return "connected" + flavor + "AndroidTestScreenshotbot";
        }
    }

    @Override
    protected @NotNull Directory getSnapshotsDir(Project project, Task task) {
        return project.getLayout().getProjectDirectory().dir("src/androidTest/screenshots");
    }
}
