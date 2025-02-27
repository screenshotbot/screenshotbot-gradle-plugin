package io.screenshotbot.gradle.plugin;

import org.gradle.internal.impldep.com.amazonaws.services.s3.transfer.Upload;
import org.gradle.internal.impldep.org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UploadCommitGraphTaskTest {
    private UploadCommitGraphTask task;

    @BeforeEach
    public void setUp() {
        this.task = new UploadCommitGraphTask(null);
    }

    @Test
    public void getArguments() {
        assertEquals(new ArrayList<>(), task.getArguments());
    }

    @Test
    public void withRepoUrl() {
        task.setRepoUrl("foobar");
        assertEquals(List.of("--repo-url", "foobar"),
                task.getArguments());
    }

}