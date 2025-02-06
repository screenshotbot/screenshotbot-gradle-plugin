package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.gradle.wrapper.Download;

import javax.inject.Inject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;

public class DownloadRecorderTask extends BaseRecorderTask {

    @Inject
    public DownloadRecorderTask(ExecOperations e) {
        super(e);
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
