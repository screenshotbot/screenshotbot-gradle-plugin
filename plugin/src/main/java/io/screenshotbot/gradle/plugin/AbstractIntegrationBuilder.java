package io.screenshotbot.gradle.plugin;

import org.gradle.api.file.Directory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardCopyOption.ATOMIC_MOVE;

public class AbstractIntegrationBuilder {
    protected void safeDelete(File file) {
        if (!file.delete()) {
            throw new RuntimeException("Could not delete: " + file);
        }
    }

    protected void restoreDir(Directory snapshotsDir) {
        String backupDir = snapshotsDir.getAsFile().toString() + "-screenshotbot-backup";

        deleteDirectory(snapshotsDir.getAsFile());

        if (new File(backupDir).exists()) {
            try {
                Files.move(Path.of(backupDir), snapshotsDir.getAsFile().toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void deleteDirectory(File asFile) {
        for (File file : asFile.listFiles()) {
            // I want to avoid having bugs here that accidently delete outside of this directory, and
            // I don't want dependencies, so I'll hardcode some of the directories I'm allowed to recurse
            // into
            if (file.isDirectory() && file.getName().equals("images")) {
                deleteDirectory(file);
            } else {
                safeDelete(file);
            }
        }
        safeDelete(asFile);
    }

    protected void backupDir(Directory dir) {
        try {
            String dest = dir.getAsFile().toString() + "-screenshotbot-backup";
            // This isn't ideal.. but if the directory already exists, let's assume it was a previous backup, so we should restore it.
            if (!new File(dest).exists()) {
                Files.move(dir.getAsFile().toPath(),
                        Path.of(dest), ATOMIC_MOVE);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
