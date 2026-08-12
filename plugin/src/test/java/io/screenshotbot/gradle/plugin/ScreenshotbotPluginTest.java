package io.screenshotbot.gradle.plugin;

import org.gradle.api.Project;
import org.gradle.internal.impldep.org.junit.Before;
import org.gradle.testfixtures.ProjectBuilder;
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

    @Test
    void removeRepoUrlFromArgs() {
        extension.setRepoUrl("bar");
        extension.setExtraArgs(List.of("--repo-url", "foobar", "--hello", "bar"));
        assertEquals("bar", extension.getRepoUrl());
        assertEquals(List.of("--hello", "bar"), extension.getExtraArgs());
    }

    @Test
    void doesntRemoveTrailingFlag() {
        extension.setExtraArgs(List.of("--repo-url"));
        assertEquals(List.of("--repo-url"), extension.getExtraArgs());
    }

    @Test
    void registersDownloadTaskWhenAppliedToRoot() {
        Project root = ProjectBuilder.builder().build();
        root.getPlugins().apply(ScreenshotbotPlugin.class);

        assertNotNull(root.getTasks().findByName("downloadScreenshotbotRecorder"));
    }

    /*
     * Without Isolated Projects a subproject still registers the task on the
     * root project, so that existing builds that only apply the plugin to their
     * modules keep working. (ProjectBuilder can't turn Isolated Projects on, so
     * the other branch isn't covered here.)
     */
    @Test
    void subprojectRegistersDownloadTaskOnTheRootProject() {
        Project root = ProjectBuilder.builder().build();
        Project child = ProjectBuilder.builder().withName("app").withParent(root).build();
        child.getPlugins().apply(ScreenshotbotPlugin.class);

        assertNotNull(root.getTasks().findByName("downloadScreenshotbotRecorder"));
        assertNull(child.getTasks().findByName("downloadScreenshotbotRecorder"));
    }

    @Test
    void onlyRegistersTheDownloadTaskOnce() {
        Project root = ProjectBuilder.builder().build();
        Project first = ProjectBuilder.builder().withName("app").withParent(root).build();
        Project second = ProjectBuilder.builder().withName("ui-toolkit").withParent(root).build();

        first.getPlugins().apply(ScreenshotbotPlugin.class);
        second.getPlugins().apply(ScreenshotbotPlugin.class);

        assertNotNull(root.getTasks().findByName("downloadScreenshotbotRecorder"));
    }
}