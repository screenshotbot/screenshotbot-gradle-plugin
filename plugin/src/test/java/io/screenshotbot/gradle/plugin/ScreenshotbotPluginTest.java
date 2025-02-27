package io.screenshotbot.gradle.plugin;

import org.gradle.internal.impldep.org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScreenshotbotPluginTest {

    private ScreenshotbotPlugin.Extension extension;

    @BeforeEach
    public void setUp() {
        extension = new ScreenshotbotPlugin.Extension();
    }

    @Test
    void ensureGetRepoUrl() {
        assertEquals(null, extension.getRepoUrl());
        extension.setRepoUrl("foo");
        assertEquals("foo", extension.getRepoUrl());
    }

    @Test
    void picksRepoUrlFromArgs() {
        extension.setExtraArgs(List.of("--repo-url", "foobar"));
        assertEquals("foobar", extension.getRepoUrl());
    }

    @Test
    void handlesBadlyFormedCaseWithoutCrashing() {
        extension.setExtraArgs(List.of("--repo-url"));
        assertEquals(null, extension.getRepoUrl());
    }

    @Test
    void prioritizeRepoUrl() {
        extension.setRepoUrl("bar");
        extension.setExtraArgs(List.of("--repo-url", "foobar"));
        assertEquals("bar", extension.getRepoUrl());
    }
}