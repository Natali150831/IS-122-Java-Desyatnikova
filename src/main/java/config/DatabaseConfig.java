package config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "config.properties";
    private static final Properties properties = new Properties();

    private static String absoluteDbPath;

    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);

            // Инициализируем абсолютный путь к файлу БД
            File dbFile = new File(properties.getProperty("db.file.path"));
            absoluteDbPath = dbFile.getAbsolutePath();

        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    public static String getAbsoluteDbPath() {
        return absoluteDbPath;
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
}