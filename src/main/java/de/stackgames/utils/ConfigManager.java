package de.stackgames.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@UtilityClass
public class ConfigManager {
    /**
     * Gets a custom configuration file. If it is not present, it will be created.
     * Needs a file in the JAR as template with the same filename.
     * Will also create the folder if not present
     * @param plugin The plugin which is requesting the config
     * @param configName The name of the config file. Should be the same as the sample file in ressources
     * @return the configuration, if found or Optional.empty() if not
     */
    public Optional<FileConfiguration> getCustomConfig(Plugin plugin, String configName) {
        File customConfigFile = new File(plugin.getDataFolder(), configName);
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            plugin.saveResource(configName, false);
        }

        FileConfiguration customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
            return Optional.of(customConfig);
        } catch (IOException | InvalidConfigurationException e) {
            plugin.getLogger().severe("An error occured while loading: " + configName);
            e.printStackTrace();
            return Optional.empty();
        }
    }

}
