package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

public class UploadScreenshotsTask extends DefaultTask {
    public File directory = null;
    public String channel = null;
    public String mode = "record";

    @TaskAction
    public void uploadScreenshots() {
        ensureLibraryInstalled();
        uploadChannel();
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

    public void uploadChannel() {
        getProject().exec((it) -> {
                it.setExecutable(System.getenv("HOME") + "/screenshotbot/recorder");
                ArrayList<String> args = new ArrayList<>();
                if (!mode.equals("ci")) {
                    args.add("dev");
                    args.add(mode);
                }

                args.add("--channel");
                args.add(channel);
                args.add("--directory");
                args.add(directory.toString());

                it.setArgs(args);
            });
    }


}
