package io.screenshotbot.gradle.plugin;

import org.gradle.api.internal.tasks.userinput.UserInputHandler;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.inject.Inject;
import org.gradle.process.*;

public class InstallScreenshotbotTask extends BaseRecorderTask {
    @Inject
    public InstallScreenshotbotTask(ExecOperations execOperations) {
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
            throw new IllegalStateException("No snapshot directories were created, this might be a bug in the screenshotbot plugin: " +
                                            String.join(", ", asStrings));
        }

        return exists.get(0);
    }

    @TaskAction
    public void installScreenshotbot() {
        UserInputHandler service = getServices().get(UserInputHandler.class);


        String question = getQuestion();
        String token = service.askQuestion(question, null);
        saveToken(token);
    }

    private void saveToken(String token) {
        List<String> args = prepareInstallCommand();
        args.add("--token");
        args.add(token);

        execOperations.exec((it) -> {
            it.setCommandLine(args);
        });
    }

    @NotNull
    private List<String> prepareInstallCommand() {
        List<String> args = prepareArgs();
        args.add(0, getExecutable());
        args.add("dev");
        args.add("install");
        return args;
    }

    @NotNull
    private String getQuestion() {
        List<String> args = prepareInstallCommand();
        args.add("--no-stdin");

        Process process = null;
        try {
            process = Runtime.getRuntime().exec(args.toArray(new String[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

        String question = reader.lines().collect(Collectors.joining("\n"));
        return question;
    }
}
