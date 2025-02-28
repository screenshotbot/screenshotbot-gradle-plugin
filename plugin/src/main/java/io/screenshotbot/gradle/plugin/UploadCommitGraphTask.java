package io.screenshotbot.gradle.plugin;

import org.gradle.api.tasks.TaskAction;
import org.gradle.process.ExecOperations;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import javax.inject.Inject;
import java.util.ArrayList;

public class UploadCommitGraphTask extends BaseRecorderTask {
    public void setMainBranch(String mainBranch) {
        this.mainBranch = mainBranch;
    }

    private String repoUrl;

    private String mainBranch;


    @Inject
    public UploadCommitGraphTask(ExecOperations e) {
        super(e);
    }

    @TaskAction
    public void uploadCommitGraph() {
        execOperations.exec((it) -> {
            it.setExecutable((getExecutable()));
            ArrayList<String> args = getArguments();

            it.setArgs(args);
        });
    }


    private ArrayList<String> getArguments() {
        ArrayList<String> args = new ArrayList<>();
        args.add("ci");
        args.add("upload-commit-graph");

        if (mainBranch != null && mainBranch.length() > 0) {
            args.add("--main-branch");
            args.add(mainBranch);
        }

        if (repoUrl != null && repoUrl.length() > 0) {
            args.add("--repo-url");
            args.add(repoUrl);
        }
        return args;
    }

    public void setRepoUrl(String repoUrl) {
        this.repoUrl = repoUrl;
    }
}
