package com.company.scheduler.common;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class TestUtil {

    private TestUtil() {
    }

    public static String json(String filename) throws IOException {
        Path path = Paths.get("src/test/resources/__files", filename);
        return Files.readString(path, UTF_8);
    }

}
