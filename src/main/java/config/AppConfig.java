package config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Application Configuration Manager
 * Manages database connection properties, environment variable overrides,
 * and external configuration files.
 */
public class AppConfig {
    private static final String DEFAULT_PROPERTIES = "/database.properties";
    private static final String EXTERNAL_CONFIG_FILE = "config.properties";

    private static String host = "localhost";
    private static int port = 3306;
    private static String database = "campus_digital_forensics";
    private static String user = "root";
    private static String password = "root";
    private static boolean allowFallbackDemo = true;

    static {
        loadConfig();
    }

    public static synchronized void loadConfig() {
        Properties props = new Properties();

        // 1. Try classpath resource
        try (InputStream is = AppConfig.class.getResourceAsStream(DEFAULT_PROPERTIES)) {
            if (is != null) {
                props.load(is);
            }
        } catch (Exception ignored) {
        }

        // 2. Try external file in working directory
        File extFile = new File(EXTERNAL_CONFIG_FILE);
        if (extFile.exists() && extFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(extFile)) {
                props.load(fis);
            } catch (Exception ignored) {
            }
        }

        // Apply loaded properties
        host = props.getProperty("db.host", host);
        try {
            port = Integer.parseInt(props.getProperty("db.port", String.valueOf(port)));
        } catch (NumberFormatException ignored) {}
        database = props.getProperty("db.name", database);
        user = props.getProperty("db.user", user);
        password = props.getProperty("db.password", password);
        allowFallbackDemo = Boolean.parseBoolean(props.getProperty("db.allowFallbackDemo", "true"));

        // 3. Environment variable overrides
        String envHost = System.getenv("CDF_DB_HOST");
        if (envHost != null && !envHost.isBlank()) host = envHost;

        String envPort = System.getenv("CDF_DB_PORT");
        if (envPort != null && !envPort.isBlank()) {
            try { port = Integer.parseInt(envPort); } catch (Exception ignored) {}
        }

        String envDb = System.getenv("CDF_DB_NAME");
        if (envDb != null && !envDb.isBlank()) database = envDb;

        String envUser = System.getenv("CDF_DB_USER");
        if (envUser != null && !envUser.isBlank()) user = envUser;

        String envPass = System.getenv("CDF_DB_PASSWORD");
        if (envPass != null) password = envPass;
    }

    public static synchronized void updateCredentials(String newHost, int newPort, String newDb, String newUser, String newPass) {
        host = newHost;
        port = newPort;
        database = newDb;
        user = newUser;
        password = newPass;
    }

    public static String getHost() { return host; }
    public static int getPort() { return port; }
    public static String getDatabase() { return database; }
    public static String getUser() { return user; }
    public static String getPassword() { return password; }
    public static boolean isAllowFallbackDemo() { return allowFallbackDemo; }

    public static String getJdbcUrl() {
        return String.format(
            "jdbc:mysql://%s:%d/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&connectTimeout=4000",
            host, port, database
        );
    }
}
