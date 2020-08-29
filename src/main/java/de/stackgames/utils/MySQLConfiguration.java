package de.stackgames.utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.ConfigurationSection;

public class MySQLConfiguration {
    @Setter
    @Getter
    private int maxPoolSize = 10;
    @Setter
    @Getter
    private int port = 3306;
    @Setter
    @Getter
    private String hostname = "localhost";
    @Setter
    @Getter
    private String database;
    @Setter
    @Getter
    private String username;
    @Setter
    @Getter
    private String password;

    /**
     * Holds MySQL configuration variables
     */
    public MySQLConfiguration() {
        // Empty
    }

    /**
     * Loads any fields present in the configuration, else keeps the default configuration
     * @param section The Configuration to load the fields from
     * @return Wether or not any field is missing
     */
    public boolean loadPresent(ConfigurationSection section) {
        if(section.isString("host")) {
            hostname = section.getString("host");
        }
        if(section.isInt("port")) {
            port = section.getInt("port");
        }
        database = section.getString("database");
        username = section.getString("username");
        password = section.getString("password");
        if(section.isInt("max_pool_size")) {
            maxPoolSize = section.getInt("max_pool_size");
        }
        return database != null && username != null && password != null;
    }
}
