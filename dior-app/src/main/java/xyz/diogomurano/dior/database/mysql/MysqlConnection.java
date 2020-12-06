package xyz.diogomurano.dior.database.mysql;

import xyz.diogomurano.dior.database.DatabaseConnection;
import xyz.diogomurano.dior.database.DatabaseCredentials;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MysqlConnection implements DatabaseConnection {

    private static final ExecutorService POOL = Executors.newFixedThreadPool(2);

    private final DatabaseCredentials credentials;
    private Connection connection;

    public MysqlConnection(DatabaseCredentials credentials) {
        this.credentials = credentials;
    }

    @Override
    public Connection getConnection() {
        if (!isConnected()) {
            setupConnection();
        }
        return connection;
    }

    @Override
    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException ignored) {
        }
        return false;
    }

    @Override
    public void setupConnection() {
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            String url =
                    "jdbc:"
                            + "mysql"
                            + "://"
                            + credentials.getHostname()
                            + "/"
                            + credentials.getDatabase()
                            + "?user="
                            + credentials.getUsername()
                            + "&password="
                            + credentials.getPassword();
            this.connection = DriverManager.getConnection(url, credentials.getUsername(), credentials.getPassword());
        } catch (ClassNotFoundException | SQLException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
    }
}
