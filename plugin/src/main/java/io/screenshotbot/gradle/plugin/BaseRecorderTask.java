package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

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

    @NotNull
    protected ArrayList<String> prepareArgs() {
        ArrayList<String> args = new ArrayList<>();

        assert (hostname != null);
        args.add("--api-hostname");
        args.add(hostname);
        return args;
    }
}
