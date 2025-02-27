package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

import javax.inject.Inject;
import org.gradle.process.*;


public class UploadScreenshotsTask extends BaseRecorderTask {
    public File directory = null;
    public String channel = null;
    public String mode = "record";
    public String batch = null;

    public List<String> extraArgs = new ArrayList<>();

    public String mainBranch = null;

    public String repoUrl = null;

    @Inject
    public UploadScreenshotsTask(ExecOperations execOperations) {
        super(execOperations);
        dependsOn(":downloadScreenshotbotRecorder");
    }

    @TaskAction
    public void uploadScreenshots() {
        uploadChannel();
        System.out.println("Uploading: " + directory + " " + channel);
    }

    public void uploadChannel() {
        execOperations.exec((it) -> {
            it.setExecutable(getExecutable());
            ArrayList<String> args = prepareArgs();

            if (!mode.equals("ci")) {
                args.add("dev");
                args.add(mode);
            } else {
                args.add("--commit-limit=0");
            }

            args.add("--recursive");
            args.add("--channel");
            args.add(channel);
            args.add("--directory");
            args.add(directory.toString());

            if (this.batch != null && this.batch.length() > 0 && mode.equals("ci")) {
                args.add("--batch");
                args.add(this.batch);
            }

            maybeAddArgument(args, "--main-branch", this.mainBranch);
            maybeAddArgument(args, "--repo-url", this.repoUrl);

            args.addAll(extraArgs);
            it.setArgs(args);
        });
    }

    private void maybeAddArgument(ArrayList<String> args, String param, String value) {
        if (value != null && value.length() > 0) {
            args.add(param);
            args.add(value);
        }
    }


}
