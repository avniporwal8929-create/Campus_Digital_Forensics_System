package config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Dedicated Database Connection Manager.
 * Manages JDBC connections to MySQL 8.0 with prepared statements support,
 * connection validation, and diagnostic error reporting.
 */
public class DBConnection {
    private static final String DRIVER_CLASS = "com.mysql.cj.jdbc.Driver";
    private static volatile boolean driverLoaded = false;
    private static String lastErrorMessage = "";
    private static boolean liveDatabaseConnected = false;

    static {
        try {
            Class.forName(DRIVER_CLASS);
            driverLoaded = true;
        } catch (ClassNotFoundException e) {
            lastErrorMessage = "MySQL JDBC Driver not found in classpath: " + e.getMessage();
        }
    }

    /**
     * Establishes a new JDBC connection to the MySQL database.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!driverLoaded) {
            try {
                Class.forName(DRIVER_CLASS);
                driverLoaded = true;
            } catch (ClassNotFoundException e) {
                lastErrorMessage = "Driver missing: " + e.getMessage();
                throw new SQLException("MySQL JDBC Driver missing from classpath", e);
            }
        }

        try {
            Connection conn = DriverManager.getConnection(
                AppConfig.getJdbcUrl(),
                AppConfig.getUser(),
                AppConfig.getPassword()
            );
            liveDatabaseConnected = true;
            lastErrorMessage = "Connected successfully to " + AppConfig.getDatabase();
            return conn;
        } catch (SQLException e) {
            liveDatabaseConnected = false;
            lastErrorMessage = formatErrorMessage(e);
            throw e;
        }
    }

    /**
     * Tests whether the database connection can be established.
     * @return true if connection succeeds, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * Diagnostic error formatter to provide actionable feedback to users.
     */
    private static String formatErrorMessage(SQLException e) {
        String msg = e.getMessage();
        int errorCode = e.getErrorCode();
        String sqlState = e.getSQLState();

        if (msg != null && (msg.contains("Communications link failure") || msg.contains("Connection refused"))) {
            return String.format("Cannot reach MySQL at %s:%d. Please ensure MySQL Server 8.0 is started.",
                AppConfig.getHost(), AppConfig.getPort());
        }
        if (errorCode == 1045 || (msg != null && msg.contains("Access denied"))) {
            return String.format("Authentication failed for user '%s'. Check database password.",
                AppConfig.getUser());
        }
        if (errorCode == 1049 || (msg != null && msg.contains("Unknown database"))) {
            return String.format("Database '%s' does not exist. Run the database_schema.sql script.",
                AppConfig.getDatabase());
        }
        return String.format("SQL Error [%d / %s]: %s", errorCode, sqlState, msg);
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    public static boolean isLiveDatabaseConnected() {
        return liveDatabaseConnected;
    }

    /**
     * Safely closes resources (ResultSet, Statement, Connection).
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                }
            }
        }
    }
}
