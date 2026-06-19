package Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class AppEnv {

    private static final Map<String, String> FILE_ENV = loadDotEnv();

    private AppEnv() {
    }

    public static String get(String key, String defaultValue) {
        String value = System.getenv(key);
        if (value == null || value.trim().isEmpty()) {
            value = FILE_ENV.get(key);
        }
        if (value == null || value.trim().isEmpty()) {
            return defaultValue;
        }
        return value.trim();
    }

    public static int getInt(String key, int defaultValue) {
        String value = get(key, "");
        if (value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static Map<String, String> loadDotEnv() {
        Map<String, String> values = new HashMap<>();
        File envFile = new File(".env");
        if (!envFile.isFile()) {
            return values;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                    continue;
                }
                int separator = trimmed.indexOf('=');
                if (separator <= 0) {
                    continue;
                }
                String key = trimmed.substring(0, separator).trim();
                String rawValue = trimmed.substring(separator + 1).trim();
                values.put(key, stripQuotes(rawValue));
            }
        } catch (IOException ex) {
            System.out.println("No se pudo leer .env: " + ex.getMessage());
        }
        return values;
    }

    private static String stripQuotes(String value) {
        if (value.length() >= 2) {
            boolean doubleQuoted = value.startsWith("\"") && value.endsWith("\"");
            boolean singleQuoted = value.startsWith("'") && value.endsWith("'");
            if (doubleQuoted || singleQuoted) {
                return value.substring(1, value.length() - 1).trim();
            }
        }
        return value;
    }
}
