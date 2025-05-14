package config;

import java.sql.*;
import java.nio.file.*;
import java.io.*;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;
import org.firebirdsql.management.FBManager;

public class DatabaseInitializer {
    private static final String INIT_SCRIPT = "/init.sql";
    private static final String DEFAULT_HOST = "localhost";
    private static final int DEFAULT_PORT = 3050;

    public void setup(String dbPath, String url, String user, String password) {
        try {
            System.out.println("Starting database initialization...");
            Path dbFilePath = Paths.get(dbPath);

            if (!Files.exists(dbFilePath)) {
                System.out.println("Database file not found, creating new database...");
                createDatabase(dbFilePath, user, password);
            }

            initializeSchema(url, user, password);
            System.out.println("Database initialization completed successfully");
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private void createDatabase(Path dbPath, String user, String password) throws Exception {
        FBManager fbManager = new FBManager();
        try {
            System.out.println("Creating Firebird database at: " + dbPath);
            fbManager.setServer(DEFAULT_HOST);
            fbManager.setPort(DEFAULT_PORT);
            fbManager.start();
            fbManager.createDatabase(dbPath.toString(), user, password);
        } finally {
            fbManager.stop();
        }
    }

    private void initializeSchema(String url, String user, String password) throws SQLException {
        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            if (!isTableExists(conn, "BOOK_INFO") || !isTableExists(conn, "BOOKS")) {
                System.out.println("Required tables not found, executing initialization script");
                executeScript(conn);
            } else {
                System.out.println("Tables already exist, skipping initialization script");
            }
        }
    }

    private boolean isTableExists(Connection conn, String tableName) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(null, null, tableName, null)) {
            return rs.next();
        }
    }

    private void executeScript(Connection conn) throws SQLException {
        InputStream is = getClass().getResourceAsStream(INIT_SCRIPT);
        if (is == null) {
            throw new SQLException("Initialization script not found: " + INIT_SCRIPT);
        }

        conn.setAutoCommit(false);
        try (Scanner scanner = new Scanner(is, StandardCharsets.UTF_8);
             Statement stmt = conn.createStatement()) {

            StringBuilder sb = new StringBuilder();
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                if (line.startsWith("--") || line.isEmpty()) {
                    continue;
                }

                sb.append(line).append(" ");

                if (line.endsWith(";")) {
                    String sql = sb.toString().replace(";", "").trim();
                    if (!sql.isEmpty()) {
                        System.out.println("Executing SQL: " + sql);
                        stmt.execute(sql);
                    }
                    sb.setLength(0);
                }
            }

            if (sb.length() > 0) {
                String sql = sb.toString().trim();
                if (!sql.isEmpty()) {
                    System.out.println("Executing final SQL: " + sql);
                    stmt.execute(sql);
                }
            }

            conn.commit();
            System.out.println("Database schema initialized successfully");
        } catch (Exception e) {
            conn.rollback();
            System.err.println("Failed to execute initialization script: " + e.getMessage());
            throw new SQLException("Failed to execute initialization script", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                System.err.println("Warning: Failed to close script input stream");
            }
        }
    }
}