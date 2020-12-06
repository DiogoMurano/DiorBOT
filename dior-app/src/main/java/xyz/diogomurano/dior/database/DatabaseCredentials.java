package xyz.diogomurano.dior.database;

import java.util.function.Consumer;

public class DatabaseCredentials {

    private boolean fileSave;

    private String hostname;
    private String username;
    private String password;
    private String database;

    private int port;

    public DatabaseCredentials() {}

    public boolean isFileSave() {
        return fileSave;
    }

    public void setFileSave(boolean fileSave) {
        this.fileSave = fileSave;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getDatabase() {
        return database;
    }

    public void setDatabase(String database) {
        this.database = database;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public DatabaseCredentials with(Consumer<DatabaseCredentials> consumer) {
        consumer.accept(this);
        return this;
    }

}
