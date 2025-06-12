package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DownloadRecorderTask extends BaseRecorderTask {

    @Inject
    public DownloadRecorderTask(ExecOperations e) {
        super(e);
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

    @TaskAction
    public void downloadRecorder() {
        execOperations.exec((it) -> {
            var reader = new BufferedReader(
                    new InputStreamReader(
                            DownloadRecorderTask.class.getResourceAsStream("/io/screenshotbot/gradle/recorder.sh")));
            var shContents = readAll(reader);

            it.setExecutable("bash");
            ArrayList<String> args = new ArrayList<String>();
            args.add("-c");
            args.add(shContents);
            it.setArgs(args);

        });
    }

    private String readAll(BufferedReader reader) {
        var result = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                result.append(line);
                result.append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result.toString();
    }
}
