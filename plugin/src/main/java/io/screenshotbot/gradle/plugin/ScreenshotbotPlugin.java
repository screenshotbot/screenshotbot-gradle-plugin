package io.screenshotbot.gradle.plugin;

import groovy.lang.Closure;
import org.gradle.api.*;
import org.gradle.api.tasks.TaskContainer;

import static org.gradle.api.plugins.JavaBasePlugin.VERIFICATION_GROUP;

public class ScreenshotbotPlugin implements Plugin<Project> {
    public static class Extension {
        String hostname = "https://api.screenshotbot.io";
    }
    @Override
    public void apply(Project target) {
        target.getExtensions().create("screenshotbot", Extension.class);

        applyPaparazziTasks(target);

    }

    private void applyPaparazziTasks(Project project) {
        Action<Plugin> action = new Action<Plugin>() {
            @Override
            public void execute(Plugin plugin) {
                project.afterEvaluate((project) -> {
                    TaskContainer tasks = project.getTasks();
                    tasks.stream().forEach((task) -> {

                        if (task.getName().startsWith("recordPaparazzi")) {
                            prepareRecordTask(task, tasks);
                        }
                    });
                });
            }
        };
        project.getPlugins().withId("app.cash.paparazzi", action);

    }

    private void prepareRecordTask(Task task, TaskContainer tasks) {
        tasks.register(task.getName() + "Screenshotbot",
                        RecordPaparazziTask.class)
                .configure((it) -> {
                    it.setGroup(VERIFICATION_GROUP);
                    it.setDescription("Records paparazzi screenshots into Screenshotbot");
                    it.doFirst((it2) -> {
                       System.out.println("ACTION!");
                    });
                    it.dependsOn(task.getName());
                });
    }
}
