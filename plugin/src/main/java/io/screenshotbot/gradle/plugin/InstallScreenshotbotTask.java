package io.screenshotbot.gradle.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.internal.tasks.userinput.UserInputHandler;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class InstallScreenshotbotTask extends BaseRecorderTask {
    @TaskAction
    public void installScreenshotbot() {
        ensureLibraryInstalled();
        UserInputHandler service = getServices().get(UserInputHandler.class);


        String question = getQuestion();
        String token = service.askQuestion(question, null);
        saveToken(token);
    }

    private void saveToken(String token) {
        List<String> args = prepareInstallCommand();
        args.add("--token");
        args.add(token);

        getProject().exec((it) -> {
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
