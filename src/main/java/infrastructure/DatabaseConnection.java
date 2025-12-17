package infrastructure;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;
/**
 * Manages a single shared connection to the database.
 *
 * <p>This class follows a singleton-like pattern for the database connection.
 * It reads the database configuration from the `db.properties` file and
 * provides a centralized way for all services to access the database.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 *     Connection conn = DatabaseConnection.getConnection();
 * </pre>
 *
 * @author Sara
 * @version 1.0
 */

public class DatabaseConnection {
    /** Shared JDBC connection instance */
    private static Connection connection;

    /**
     * Returns the shared database connection.
     * If the connection does not exist or is closed, it will be created
     * using the properties defined in `db.properties`.
     *
     * @return the JDBC connection
     * @throws Exception if an error occurs while reading properties
     *                   or connecting to the database
     */
    public static Connection getConnection() throws Exception {
        if (connection == null || connection.isClosed()) {
            Properties props = new Properties();
            try (InputStream input = DatabaseConnection.class
                    .getClassLoader()
                    .getResourceAsStream("db.properties")) {
                props.load(input);
            }

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.username");
            String pass = props.getProperty("db.password");
            connection = DriverManager.getConnection(url, user, pass);
        }
        return connection;
    }
}
