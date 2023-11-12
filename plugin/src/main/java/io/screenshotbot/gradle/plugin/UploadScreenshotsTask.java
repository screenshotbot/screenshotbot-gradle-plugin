package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

public class UploadScreenshotsTask extends DefaultTask {
    public File directory = null;
    public String channel = null;

    @TaskAction
    public void uploadScreenshots() {
        System.out.println("Uploading: " + directory + " " + channel);
    }
}
