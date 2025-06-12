package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.util.*;

import javax.inject.Inject;
import org.gradle.process.*;


public class UploadScreenshotsTask extends BaseRecorderTask {
    public List<File> possibleDirectories = null;
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
            return null;
        }

        return exists.get(0);
    }

    @TaskAction
    public void uploadScreenshots() {
        uploadChannel();
        System.out.println("Uploading: " + possibleDirectories + " " + channel);
    }

    public void uploadChannel() {
        execOperations.exec((it) -> {
            File snapshotDir = getOnlyExistingDir(possibleDirectories);

            if (snapshotDir == null) {
                getLogger().warn("No screenshots were generated in this module");
                return;
            }

            it.setExecutable(getExecutable());
            ArrayList<String> args = prepareArgs();

            if (!mode.equals("ci")) {
                args.add("dev");
                args.add(mode);
            }

            args.add("--recursive");
            args.add("--channel");
            args.add(channel);
            args.add("--directory");
            args.add(snapshotDir.toString());

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
