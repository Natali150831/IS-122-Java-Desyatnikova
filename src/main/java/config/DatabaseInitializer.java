package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.InputStream;

public class DatabaseInitializer {
    private static final String INIT_SCRIPT = "init.sql";

    public static void initializeDatabase() {
        String dbPath = DatabaseConfig.getAbsoluteDbPath();

        try {
            // Проверяем существование файла БД
            if (!Files.exists(Paths.get(dbPath))) {
                createDatabase();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void createDatabase() throws SQLException {
        try (Connection connection = DriverManager.getConnection(
                DatabaseConfig.getFullDatabaseUrl() + "?create=true", // если базы нет по этому пути, то создать её
                DatabaseConfig.getUser(),
                DatabaseConfig.getPassword());
             Statement statement = connection.createStatement()) {

            // Читаем SQL скрипт из ресурсов
            InputStream is = DatabaseInitializer.class.getClassLoader().getResourceAsStream(INIT_SCRIPT);
            if (is == null) {
                throw new RuntimeException("Initialization SQL script not found: " + INIT_SCRIPT);
            }

            String sql = new String(is.readAllBytes());

            // Выполняем SQL скрипт
            statement.execute(sql);
        } catch (Exception e) {
            throw new SQLException("Failed to create database", e);
        }
    }
}