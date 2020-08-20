package de.stackgames.utils;

import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.IOUtils;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.MissingFormatArgumentException;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Utility class for MySQL connections using HikariCP. Should always be shaded and relocated.
 */
@UtilityClass
public class MySQLConnection {

    private HikariDataSource hikari;
    private Plugin plugin;
    private Logger logger;

    private void openConnection(FileConfiguration fileConfiguration, MySQLConfiguration sqlConfiguration) {
        if(!sqlConfiguration.loadPresent(fileConfiguration)) throw new MissingFormatArgumentException("mysql.yml is missing some settings, unable to connect");
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        hikari.addDataSourceProperty("serverName", sqlConfiguration.getHostname());
        hikari.addDataSourceProperty("port", sqlConfiguration.getPort());
        hikari.addDataSourceProperty("databaseName", sqlConfiguration.getDatabase());
        hikari.addDataSourceProperty("user", sqlConfiguration.getUsername());
        hikari.addDataSourceProperty("password", sqlConfiguration.getPassword());
        hikari.setMaximumPoolSize(sqlConfiguration.getMaxPoolSize());
    }

    /**
     * Opens the connection to the database and checks/creates needed tables
     *
     * @param proxyPlugin   The plugin which requests the connection pool
     * @param defaultConfig The default SQL configuration. At least the pool size should be set reasonable.
     * @return true if successful, false if not. If it returns false, the plugin should probably stop.
     */
    public boolean init(net.md_5.bungee.api.plugin.Plugin proxyPlugin, MySQLConfiguration defaultConfig) {
        if(MySQLConnection.class.getPackage().getName().equals("de.stackgames.utils")) {
            throw new InvalidCodeException("This class should be relocated, but it isn't!");
        }
        MySQLConnection.logger = proxyPlugin.getLogger();
        return initConnection(defaultConfig, ConfigManager.getCustomConfig(proxyPlugin, "mysql.yml"));
    }

    /**
     * Opens the connection to the database and checks/creates needed tables
     *
     * @param spigotPlugin  The plugin which requests the connection pool
     * @param defaultConfig The default SQL configuration. At least the pool size should be set reasonable.
     * @return true if successful, false if not. If it returns false, the plugin should probably stop.
     */
    public boolean init(Plugin spigotPlugin, MySQLConfiguration defaultConfig) {
        if(MySQLConnection.class.getPackage().getName().equals("de.stackgames.utils")) {
            throw new InvalidCodeException("This class should be relocated, but it isn't!");
        }
        MySQLConnection.logger = spigotPlugin.getLogger();
        return initConnection(defaultConfig, ConfigManager.getCustomConfig(spigotPlugin, "mysql.yml"));
    }

    private static boolean initConnection(MySQLConfiguration defaultConfig, Optional<FileConfiguration> customConfig) {
        Optional<FileConfiguration> configOpt = customConfig;
        if(!configOpt.isPresent()) {
            logger.severe("UnabNenmle to load mysql.yml");
            return false;
        }
        FileConfiguration config = configOpt.get();
        openConnection(config, defaultConfig);
        try {
            Connection con = hikari.getConnection();
            URL fileUri = MySQLConnection.class.getClassLoader().getResource("database.sql");
            if(fileUri == null) {
                throw new IOException("database.sql is missing");
            }
            String sqlCreate = IOUtils.toString(fileUri, StandardCharsets.UTF_8);
            PreparedStatement pst = createPreparedStatement(con, sqlCreate);
            pst.execute();
            con.close();
            logger.info("Connected to the database");
            return true;
        } catch(SQLException e) {
            logger.severe("An error occured during database creation");
            e.printStackTrace();
            return false;
        } catch(IOException e) {
            logger.severe("An error occured during database creation, contact the plugin developer for help");
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Closes all connections. Should only be issued as cleanup.
     */
    public void closePool() {
        if(hikari != null && !hikari.isClosed()) {
            hikari.close();
        }
    }

    /**
     * Gets a Connection from the Hikari-Pool.
     *
     * @return Returns a Connection
     * @throws SQLException Throws SQLException when something goes wrong
     * @deprecated Use {@link #doWithConnectionAsync(String, ConnectionCallback)} or {@link #doWithConnectionSync(String, ConnectionCallback)} instead.
     */
    @Deprecated
    public Connection getConnection() throws SQLException {
        return hikari.getConnection();
    }

    /**
     * Create a PreparedStatement from a hikari connection
     *
     * @param sqlString The SQL-String the statement should be created with
     * @return Returns a PreparedStatement
     * @throws SQLException Throws SQLException when something goes wrong
     * @deprecated Use {@link #doWithConnectionAsync(String, ConnectionCallback)} or {@link #doWithConnectionSync(String, ConnectionCallback)} instead and create a prepared statement there.
     */
    @Deprecated
    public PreparedStatement createPreparedStatement(Connection con, String sqlString) throws SQLException {
        return con.prepareStatement(sqlString);
    }

    /**
     * Gets a connection from the connection pool and allows to execute a query in it.
     * Automatically closes the connection and handles SQLExceptions.
     *
     * @param actionSummary      A quick summary of the action. Is getting logged if an SQLException occurs and will be used as thread name.
     * @param connectionCallable An asynchronous callable where the connection can be used
     */
    public void doWithConnectionAsync(String actionSummary, MySQLConnection.ConnectionCallback connectionCallable) {
        new Thread(() -> {
            doWithConnectionSync(actionSummary, connectionCallable);
        }, plugin.getName() + "-" + actionSummary + "-Thread").start();
    }

    /**
     * Gets a connection from the connection pool and allows to execute a query in it.
     * Automatically closes the connection and handles SQLExceptions.
     *
     * @param actionSummary      A quick summary of the action. Is getting logged if an SQLException occurs.
     * @param connectionCallable An synchronous callable where the connection can be used
     */
    public void doWithConnectionSync(String actionSummary, MySQLConnection.ConnectionCallback connectionCallable) {
        try (Connection connection = hikari.getConnection()) {
            connectionCallable.doInConnection(connection);
        } catch (SQLException ex) {
            plugin.getLogger().severe("Error during database action: " + actionSummary);
            ex.printStackTrace();
        }
    }

    /**
     * Connection callback for {@link #doWithConnectionAsync(String, ConnectionCallback)} and {@link #doWithConnectionSync(String, ConnectionCallback)}
     */
    public interface ConnectionCallback {
        void doInConnection(Connection connection) throws SQLException;
    }

    private class InvalidCodeException extends RuntimeException {
        public InvalidCodeException(String s) {
            super(s);
        }
    }
}
