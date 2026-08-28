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
     * The directory that the recorder is installed into. We anchor this at the
     * root of the build, since :downloadScreenshotbotRecorder is registered on
     * the root project and every module has to agree on where the binary lives.
     * This is passed to the installer as SCREENSHOTBOT_DIR.
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
        // Deliberately not rootProject.layout.buildDirectory: reading another
        // project's layout is forbidden under Isolated Projects. rootDir is
        // allowed, and it gives every module the same answer. (GitHub #5)
        return new File(new File(getProject().getRootDir(), "build"), "screenshotbot")
                .getAbsolutePath();
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

        if (hostname != null && !hostname.isEmpty()) {
            // If unset, the CLI figures out the hostname from the API secret.
            args.add("--api-hostname");
            args.add(hostname);
        }
        return args;
    }
}
