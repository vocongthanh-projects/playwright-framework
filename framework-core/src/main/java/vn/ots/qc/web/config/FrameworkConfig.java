package vn.ots.qc.web.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

/** Immutable layered configuration with no application-specific settings. */
public final class FrameworkConfig {
    private static final String FRAMEWORK_DEFAULTS = "/config/framework.properties";
    private static final String APPLICATION_DEFAULTS = "/config/application.properties";
    private static final String CONFIG_FILE_PROPERTY = "config.file";
    private static final String CONFIG_FILE_ENV = "CONFIG_FILE";

    private final Properties values;
    private final Map<String, String> environment;
    private final Properties systemProperties;

    private FrameworkConfig(Properties values, Map<String, String> environment, Properties systemProperties) {
        this.values = values;
        this.environment = environment;
        this.systemProperties = systemProperties;
    }

    public static FrameworkConfig load() {
        return load(System.getProperties(), System.getenv());
    }

    /** Returns an immutable view with explicit values taking highest precedence. */
    public FrameworkConfig withOverrides(Map<String, String> overrides) {
        Properties overridden = new Properties();
        overridden.putAll(systemProperties);
        overrides.forEach((key, value) -> {
            if (value != null) {
                overridden.setProperty(key, value);
            }
        });
        return new FrameworkConfig(copy(values), environment, overridden);
    }

    static FrameworkConfig load(Properties systemProperties, Map<String, String> environment) {
        Properties merged = new Properties();
        try (InputStream defaults = FrameworkConfig.class.getResourceAsStream(FRAMEWORK_DEFAULTS)) {
            if (defaults == null) {
                throw new ConfigurationException("Classpath resource not found: " + FRAMEWORK_DEFAULTS);
            }
            merged.load(defaults);
        } catch (IOException exception) {
            throw new ConfigurationException("Cannot load " + FRAMEWORK_DEFAULTS, exception);
        }

        try (InputStream application = FrameworkConfig.class.getResourceAsStream(APPLICATION_DEFAULTS)) {
            if (application != null) {
                merged.load(application);
            }
        } catch (IOException exception) {
            throw new ConfigurationException("Cannot load " + APPLICATION_DEFAULTS, exception);
        }

        String externalPath = firstNonBlank(
                systemProperties.getProperty(CONFIG_FILE_PROPERTY),
                environment.get(CONFIG_FILE_ENV)
        );
        if (externalPath != null) {
            Path path = Path.of(externalPath).toAbsolutePath().normalize();
            if (!Files.isRegularFile(path)) {
                throw new ConfigurationException("Config file does not exist: " + path);
            }
            try (InputStream override = Files.newInputStream(path)) {
                merged.load(override);
            } catch (IOException exception) {
                throw new ConfigurationException("Cannot load config file: " + path, exception);
            }
        }

        Properties systemSnapshot = new Properties();
        systemSnapshot.putAll(systemProperties);
        return new FrameworkConfig(merged, Map.copyOf(environment), systemSnapshot);
    }

    public String get(String key) {
        String systemValue = systemProperties.getProperty(key);
        if (systemValue != null && !systemValue.isBlank()) {
            return systemValue.trim();
        }
        String environmentValue = environment.get(toEnvironmentKey(key));
        if (environmentValue != null && !environmentValue.isBlank()) {
            return environmentValue.trim();
        }
        return values.getProperty(key, "").trim();
    }

    public String get(String key, String defaultValue) {
        String value = get(key);
        return value.isBlank() ? defaultValue : value;
    }

    public String required(String key) {
        String value = get(key);
        if (value.isBlank()) {
            throw new ConfigurationException("Missing required setting: " + key);
        }
        return value;
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key, Integer.toString(defaultValue));
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be an integer, but was: " + value, exception);
        }
    }

    public int getInt(String key) {
        String value = required(key);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be an integer, but was: " + value, exception);
        }
    }

    public double getDouble(String key) {
        String value = required(key);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be a decimal number, but was: " + value,
                    exception);
        }
    }

    public double getDouble(String key, double defaultValue) {
        String value = get(key, Double.toString(defaultValue));
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be a decimal number, but was: " + value,
                    exception);
        }
    }

    public long getLong(String key, long defaultValue) {
        String value = get(key, Long.toString(defaultValue));
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            throw new ConfigurationException("Setting " + key + " must be a long integer, but was: " + value,
                    exception);
        }
    }

    public List<String> getList(String key) {
        return Stream.of(get(key).split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    public Optional<Path> optionalPath(String key) {
        String value = get(key);
        return value.isBlank() ? Optional.empty() : Optional.of(Path.of(value).toAbsolutePath().normalize());
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key, Boolean.toString(defaultValue)).toLowerCase(Locale.ROOT);
        if (!value.equals("true") && !value.equals("false")) {
            throw new ConfigurationException("Setting " + key + " must be true or false, but was: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public boolean getBoolean(String key) {
        String value = required(key).toLowerCase(Locale.ROOT);
        if (!value.equals("true") && !value.equals("false")) {
            throw new ConfigurationException("Setting " + key + " must be true or false, but was: " + value);
        }
        return Boolean.parseBoolean(value);
    }

    public Duration defaultTimeout() {
        return Duration.ofMillis(getInt("timeout.default"));
    }

    public Duration navigationTimeout() {
        return Duration.ofMillis(getInt("timeout.navigation"));
    }

    public Path requiredPath(String key) {
        return Path.of(required(key)).toAbsolutePath().normalize();
    }

    private static String toEnvironmentKey(String key) {
        return key.replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                .replaceAll("[^A-Za-z0-9]", "_")
                .toUpperCase(Locale.ROOT);
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }

    private static Properties copy(Properties source) {
        Properties copy = new Properties();
        copy.putAll(source);
        return copy;
    }
}
