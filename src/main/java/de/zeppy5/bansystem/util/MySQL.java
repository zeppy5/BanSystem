package de.zeppy5.bansystem.util;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.logging.Level;

public class MySQL {

    private final String HOST;

    private final String DATABASE;

    private final String USER;

    private final String PASSWORD;

    private final int PORT;

    private Connection connection;

    public MySQL(String host, String database, String user, String password, int port) {
        this.HOST = host;
        this.DATABASE = database;
        this.USER = user;
        this.PASSWORD = password;
        this.PORT = port;

        connect();
        setup();
    }

    public void connect() {
        try {
            String url = "jdbc:mysql://" +
                    HOST + ":" +
                    PORT + "/" +
                    DATABASE + "?autoReconnect=true";
            connection = DriverManager.getConnection(url, USER, PASSWORD);
            Bukkit.getLogger().log(Level.INFO, "[MySQL] Connected");
        } catch (SQLException e) {
            Bukkit.getLogger().log(Level.SEVERE, "[MySQL] Connection failed: " + e.getMessage() + " Is the Config configured correctly?");
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void setup() {
        try {
            PreparedStatement st = connection.prepareStatement("CREATE TABLE IF NOT EXISTS bans(" +
                    "UUID VARCHAR(100)," +
                    " ID VARCHAR(100)," +
                    " DATE BIGINT," +
                    " LENGTH BIGINT," +
                    " EXPIRES BIGINT," +
                    " REASON VARCHAR(200)," +
                    " STATUS INT," +
                    " BANNED_BY VARCHAR(100))");
            st.executeUpdate();
            st.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
