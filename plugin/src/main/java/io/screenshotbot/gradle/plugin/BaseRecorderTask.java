package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BaseRecorderTask extends DefaultTask {
    public String hostname = null;

    @NotNull
    protected static String getExecutable() {
        return System.getenv("HOME") + "/screenshotbot/recorder";
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

    @NotNull
    protected ArrayList<String> prepareArgs() {
        ArrayList<String> args = new ArrayList<>();

        assert (hostname != null);
        args.add("--api-hostname");
        args.add(hostname);
        return args;
    }
}
