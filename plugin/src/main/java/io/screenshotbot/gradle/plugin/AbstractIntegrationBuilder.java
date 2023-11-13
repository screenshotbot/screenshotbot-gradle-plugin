package io.screenshotbot.gradle.plugin;

import java.io.File;

public class AbstractIntegrationBuilder {
    protected void safeDelete(File file) {
        if (!file.delete()) {
            throw new RuntimeException("Could not delete: " + file);
        }
    }
}
