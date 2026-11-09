package org.delcom.usernamepbo2026springbootcashflow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Memuat file .env ke system properties sebelum Spring Boot start.
 * Nilai yang sudah ada di system property / environment tidak ditimpa.
 */
public final class DotEnv {

    private DotEnv() {
    }

    public static void load() throws IOException {
        load(Path.of(".env"));
    }

    public static void load(Path file) throws IOException {
        if (!Files.exists(file)) {
            return;
        }
        List<String> lines = Files.readAllLines(file);
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int idx = line.indexOf('=');
            if (idx <= 0) {
                continue;
            }
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1).trim();
            if (value.length() >= 2
                    && ((value.startsWith("\"") && value.endsWith("\""))
                    || (value.startsWith("'") && value.endsWith("'")))) {
                value = value.substring(1, value.length() - 1);
            }
            if (System.getProperty(key) == null && System.getenv(key) == null) {
                System.setProperty(key, value);
            }
        }
    }
}
