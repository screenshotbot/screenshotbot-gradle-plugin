package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;
import java.util.ArrayList;

public class DownloadRecorderTask extends BaseRecorderTask {

    @Inject
    public DownloadRecorderTask(ExecOperations e) {
        super(e);
    }

    @TaskAction
    public void downloadRecorder() {
        execOperations.exec((it) -> {
                it.setExecutable("bash");
                ArrayList<String> args = new ArrayList<String>();
                args.add("-c");
                args.add("curl https://cdn.screenshotbot.io/recorder.sh | sh");
                it.setArgs(args);

            });
    }
}
