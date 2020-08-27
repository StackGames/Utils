package de.stackgames.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@UtilityClass
public class ConfigManager {

    /**
     * Gets a custom configuration file. If it is not present, it will be created.
     * Needs a file in the JAR as template with the same filename.
     * Will also create the folder if not present
     * (Bukkit version)
     *
     * @param plugin     The plugin which is requesting the config
     * @param configName The name of the config file. Should be the same as the sample file in ressources
     * @return the configuration, if found or Optional.empty() if not
     */
    public Optional<FileConfiguration> getCustomConfig(Plugin plugin, String configName) {
        File customConfigFile = new File(plugin.getDataFolder(), configName);
        if(!customConfigFile.exists()) {
            plugin.saveResource(configName, false);
        }
        return getFileConfiguration(configName, customConfigFile, plugin.getLogger());
    }

    /**
     * Gets a custom configuration file. If it is not present, it will be created.
     * Needs a file in the JAR as template with the same filename.
     * Will also create the folder if not present
     * (BungeeCord version)
     *
     * @param plugin     The plugin which is requesting the config
     * @param configName The name of the config file. Should be the same as the sample file in ressources
     * @return the configuration, if found or Optional.empty() if not
     */
    public Optional<FileConfiguration> getCustomConfig(net.md_5.bungee.api.plugin.Plugin plugin, String configName) {
        File customConfigFile = new File(plugin.getDataFolder(), configName);
        if(!customConfigFile.exists()) {
            saveResource(plugin.getDataFolder(), configName, plugin.getLogger());
        }
        return getFileConfiguration(configName, customConfigFile, plugin.getLogger());
    }

    /**
     * Gets a custom configuration file. If it is not present, it will be created.
     * Needs a file in the JAR as template with the same filename.
     * Will also create the folder if not present
     * (BungeeCord version)
     *
     * @param dataFolder The folder where the config should reside
     * @param logger     The logger which should be used in case of errors
     * @param configName The name of the config file. Should be the same as the sample file in ressources
     * @return the configuration, if found or Optional.empty() if not
     */
    public Optional<FileConfiguration> getCustomConfig(File dataFolder, Logger logger, String configName) {
        File customConfigFile = new File(dataFolder, configName);
        if(!customConfigFile.exists()) {
            saveResource(dataFolder, configName, logger);
        }
        return getFileConfiguration(configName, customConfigFile, logger);
    }

    private Optional<FileConfiguration> getFileConfiguration(String configName, File customConfigFile, Logger logger) {
        FileConfiguration customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
            return Optional.of(customConfig);
        } catch(IOException | InvalidConfigurationException e) {
            logger.severe("An error occured while loading: " + configName);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Copied from upstream project
     * https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/browse/src/main/java/org/bukkit/plugin/java/JavaPlugin.java
     */
    private void saveResource(File dataFolder, String resourcePath, Logger logger) {
        if(resourcePath == null || resourcePath.equals("")) {
            throw new IllegalArgumentException("ResourcePath cannot be null or empty");
        }

        resourcePath = resourcePath.replace('\\', '/');
        InputStream in = getResource(resourcePath);
        if(in == null) {
            throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found in " + dataFolder);
        }

        File outFile = new File(dataFolder, resourcePath);
        int lastIndex = resourcePath.lastIndexOf('/');
        File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

        if(!outDir.exists()) {
            outDir.mkdirs();
        }

        if(!outFile.exists()) {
            try(OutputStream out = new FileOutputStream(outFile)) {
                byte[] buf = new byte[1024];
                int len;
                while((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
            } catch(IOException ex) {
                logger.log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
            }
        } else {
            logger.log(Level.WARNING, "Could not save " + outFile.getName() + " to " + outFile + " because " + outFile.getName() + " already exists.");
        }
    }

    private InputStream getResource(String filename) {
        if(filename == null) {
            throw new IllegalArgumentException("Filename cannot be null");
        }

        try {
            URL url = ConfigManager.class.getClassLoader().getResource(filename);

            if(url == null) {
                return null;
            }

            URLConnection connection = url.openConnection();
            connection.setUseCaches(false);
            return connection.getInputStream();
        } catch(IOException ex) {
            return null;
        }
    }

}
