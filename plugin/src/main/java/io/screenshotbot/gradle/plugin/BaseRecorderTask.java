package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.file.Directory;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.gradle.process.*;

public class BaseRecorderTask extends DefaultTask {
    public String hostname = null;


    public ExecOperations execOperations = null;

    public BaseRecorderTask(ExecOperations e) {
        execOperations = e;
    }

    @NotNull
    protected static String getExecutable() {
        String override = System.getenv("SCREENSHOTBOT_RECORDER_OVERRIDE");
        if (override != null) {
            // Mostly used for testing
            return override;
        }
        return System.getenv("HOME") + "/screenshotbot/recorder";
    }

    public static File getOnlyExistingDir(List<File> snapshotsDirList) {
        List<File> exists = new ArrayList<>();
        List<String> asStrings = new ArrayList<>(); // for debugging
        for (var dir : snapshotsDirList) {
            if (dir.exists()) {
                exists.add(dir);
            }
            asStrings.add(dir.toString());
        }

        if (exists.size() > 1) {
            throw new IllegalStateException("Too many snapshot directories created, this is a bug in the screenshotbot plugin");
        }

        if (exists.size() == 0) {
            throw new IllegalStateException("No snapshot directories were created, this might be a bug in the screenshotbot plugin: " +
                                            String.join(", ", asStrings));
        }

        return exists.get(0);
    }

    @NotNull
    protected ArrayList<String> prepareArgs() {
        ArrayList<String> args = new ArrayList<>();

        assert (hostname != null);
        args.add("--api-hostname");
        args.add(hostname);
        return args;
    }
}
