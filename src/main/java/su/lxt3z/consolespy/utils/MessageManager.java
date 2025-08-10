package su.lxt3z.consolespy.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final Map<String, String> messages = new HashMap<>();
    private final Plugin plugin;

    public MessageManager(Plugin plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    private void loadMessages() {
        File file = new File(plugin.getDataFolder(), "messages.yml");
        if (!file.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        config.getKeys(false).forEach(key -> {
            String message = config.getString(key);
            if (message != null) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', message));
            }
        });
    }

    public String getMessage(String key) {
        return messages.getOrDefault(key, "&c[The message was not found :( : " + key + "]");
    }
}