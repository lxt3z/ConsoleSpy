package su.lxt3z.consolespy.data;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerData {
    private final File dataFile;
    private final YamlConfiguration data;
    private final Set<String> spys = ConcurrentHashMap.newKeySet();
    private final Set<String> hiddenPlayers = ConcurrentHashMap.newKeySet();
    private final Map<String, Set<String>> ignoredPlayers = new ConcurrentHashMap<>();

    public PlayerData(Plugin plugin) {
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.data = YamlConfiguration.loadConfiguration(dataFile);
        loadData();
    }

    public void loadData() {
        spys.clear();
        hiddenPlayers.clear();
        ignoredPlayers.clear();

        if (data.contains("spys")) {
            spys.addAll(data.getStringList("spys"));
        }
        if (data.contains("hidden_players")) {
            hiddenPlayers.addAll(data.getStringList("hidden_players"));
        }
        if (data.contains("ignored_players")) {
            Objects.requireNonNull(data.getConfigurationSection("ignored_players"))
                    .getKeys(false)
                    .forEach(key -> ignoredPlayers.computeIfAbsent(key, k -> ConcurrentHashMap.newKeySet())
                            .addAll(data.getStringList("ignored_players." + key)));
        }
    }

    public void saveData() {
        data.set("spys", new ArrayList<>(spys));
        data.set("hidden_players", new ArrayList<>(hiddenPlayers));
        ignoredPlayers.forEach((key, set) -> data.set("ignored_players." + key, new ArrayList<>(set)));
        try {
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isIgnored(String spyName, String targetName) {
        return ignoredPlayers.getOrDefault(spyName, Collections.emptySet()).contains(targetName);
    }

    public void ignore(String spyName, String targetName) {
        ignoredPlayers.computeIfAbsent(spyName, k -> ConcurrentHashMap.newKeySet()).add(targetName);
    }

    public void unignore(String spyName, String targetName) {
        Set<String> targets = ignoredPlayers.get(spyName);
        if (targets != null) {
            targets.remove(targetName);
        }
    }

    public void addSpy(String name) {
        spys.add(name);
    }

    public void removeSpy(String name) {
        spys.remove(name);
    }

    public boolean isSpying(String name) {
        return spys.contains(name);
    }

    public void addHidden(String name) {
        hiddenPlayers.add(name);
    }

    public void removeHidden(String name) {
        hiddenPlayers.remove(name);
    }

    public boolean isHidden(String name) {
        return hiddenPlayers.contains(name);
    }

    public List<String> getSpys() {
        return new ArrayList<>(spys);
    }
}