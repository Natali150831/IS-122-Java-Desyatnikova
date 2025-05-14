package config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();
    private static String absoluteDbPath;
    private static DatabaseInitializer initializer = new DatabaseInitializer();

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
            absoluteDbPath = new File(properties.getProperty("db.file.path")).getAbsolutePath();

            // Инициализация БД
            initializer.setup(
                    getAbsoluteDbPath(),
                    getFullDatabaseUrl(),
                    getUser(),
                    getPassword()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    public static String getUrl() {
        return properties.getProperty("db.url");
    }

    public static String getUser() {
        return properties.getProperty("db.user");
    }

    public static String getPassword() {
        return properties.getProperty("db.password");
    }

    public static String getFullDatabaseUrl() {
        return getUrl() + absoluteDbPath;
    }

    public static String getAbsoluteDbPath() {
        return absoluteDbPath;
    }
}