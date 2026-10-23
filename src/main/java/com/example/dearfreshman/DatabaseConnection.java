package com.example.dearfreshman;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    // SQLite will create a local file named 'dear_freshman.db' in the game directory
    private static final String URL = "jdbc:sqlite:dear_freshman.db";

    private DatabaseConnection() {
        // Prevent creating instances of this utility class.
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found.", e);
        }

        Connection connection = DriverManager.getConnection(URL);

        // Ensure foreign key constraints are enforced in SQLite
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }

        return connection;
    }

    // Automatically generates the database tables if they do not exist yet
    public static void initializeDatabase() {
        // FIXED: Using standard SQLite INTEGER PRIMARY KEY syntax to avoid linter conflicts
        String createPlayersTable = """
                CREATE TABLE IF NOT EXISTS players (
                    player_id INTEGER PRIMARY KEY,
                    player_name TEXT NOT NULL,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;

        String createGameSavesTable = """
                CREATE TABLE IF NOT EXISTS game_saves (
                    save_id INTEGER PRIMARY KEY,
                    player_id INTEGER NOT NULL,
                    save_name TEXT,
                    current_chapter INTEGER,
                    current_event_index INTEGER,
                    game_day INTEGER,
                    game_minutes INTEGER,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (player_id) REFERENCES players(player_id) ON DELETE CASCADE
                );
                """;

        String createPlayerStatsTable = """
                CREATE TABLE IF NOT EXISTS player_stats (
                    stat_id INTEGER PRIMARY KEY,
                    save_id INTEGER NOT NULL,
                    energy INTEGER,
                    friendship INTEGER,
                    reputation INTEGER,
                    academics INTEGER,
                    suspicion INTEGER,
                    gino_trust INTEGER,
                    z_trust INTEGER,
                    obet_trust INTEGER,
                    william_suspicion INTEGER,
                    programming_score INTEGER,
                    database_score INTEGER,
                    history_score INTEGER,
                    quiz_answered INTEGER,
                    FOREIGN KEY (save_id) REFERENCES game_saves(save_id) ON DELETE CASCADE
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createPlayersTable);
            stmt.execute(createGameSavesTable);
            stmt.execute(createPlayerStatsTable);
            System.out.println("SQLite database tables initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize SQLite database tables: " + e.getMessage());
        }
    }

    public static boolean testConnection() {
        try (Connection connection = getConnection()) {
            System.out.println("Successfully connected to SQLite database!");
            return true;
        } catch (SQLException e) {
            System.err.println("Failed to connect to SQLite database.");
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}