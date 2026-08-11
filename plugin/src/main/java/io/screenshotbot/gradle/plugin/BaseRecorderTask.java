package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;

import org.gradle.process.*;

public class BaseRecorderTask extends DefaultTask {
    public String hostname = null;


    public ExecOperations execOperations = null;

    /*
     * The directory that the recorder is installed into. We use the root
     * project's build directory, since :downloadScreenshotbotRecorder is
     * registered on the root project and every module has to agree on where the
     * binary lives. This is passed to the installer as SCREENSHOTBOT_DIR.
     */
    private final String screenshotbotDir;

    public BaseRecorderTask(ExecOperations e) {
        execOperations = e;
        screenshotbotDir = computeScreenshotbotDir();
    }

    private String computeScreenshotbotDir() {
        String override = System.getenv("SCREENSHOTBOT_DIR");
        if (override != null && !override.isEmpty()) {
            return override;
        }
        return getProject().getRootProject().getLayout().getBuildDirectory()
                .dir("screenshotbot").get().getAsFile().getAbsolutePath();
    }

    @Internal
    @NotNull
    public String getScreenshotbotDir() {
        return screenshotbotDir;
    }

    @Internal
    @NotNull
    protected String getExecutable() {
        String override = System.getenv("SCREENSHOTBOT_RECORDER_OVERRIDE");
        if (override != null) {
            // Mostly used for testing
            return override;
        }
        return new File(screenshotbotDir, "recorder").toString();
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
