package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

public class UploadScreenshotsTask extends BaseRecorderTask {
    public File directory = null;
    public String channel = null;
    public String mode = "record";

    @TaskAction
    public void uploadScreenshots() {
        ensureLibraryInstalled();
        uploadChannel();
        System.out.println("Uploading: " + directory + " " + channel);
    }

    public void uploadChannel() {
        getProject().exec((it) -> {
                it.setExecutable(getExecutable());
            ArrayList<String> args = prepareArgs();

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
