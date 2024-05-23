package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;

import javax.inject.Inject;

public class DownloadRecorderTask extends BaseRecorderTask {

    @Inject
    public DownloadRecorderTask(ExecOperations e) {
        super(e);
    }

    @TaskAction
    public void downloadRecorder() {
        ensureLibraryInstalled();
    }
}
