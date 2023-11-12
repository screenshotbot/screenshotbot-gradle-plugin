package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

public class UploadScreenshotsTask extends DefaultTask {
    public File directory = null;
    public String channel = null;

    @TaskAction
    public void uploadScreenshots() {
        ensureLibraryInstalled();
        System.out.println("Uploading: " + directory + " " + channel);
    }

    public void ensureLibraryInstalled() {
        getProject().exec((it) -> {
                it.setExecutable("bash");
                ArrayList<String> args = new ArrayList<String>();
                args.add("-c");
                args.add("curl https://screenshotbot.io/recorder.sh | sh");
                it.setArgs(args);

            });
    }


}
