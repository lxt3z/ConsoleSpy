package su.lxt3z.consolespy.data;

public class PlayerData {
    private final Main plugin;
    private final File dataFile;

    private final ConcurrentHashMap<UUID, Boolean> spyingPlayers;
    private final ConcurrentHashMap<UUID, Boolean> hidingPlayers;
    private final Set<UUID> ignoredPlayers;

    private final ConcurrentHashMap<UUID, String> playerNameCache;

    private volatile boolean dataChanged = false;

    public PlayerData(Main plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "data.yml");
        this.spyingPlayers = new ConcurrentHashMap<>();
        this.hidingPlayers = new ConcurrentHashMap<>();
        this.ignoredPlayers = ConcurrentHashMap.newKeySet();
        this.playerNameCache = new ConcurrentHashMap<>();
        loadData();
    }

    private void loadData() {
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            saveDataSync();
            return;
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(dataFile);
        spyingPlayers.clear();
        hidingPlayers.clear();
        ignoredPlayers.clear();
        playerNameCache.clear();

        for (String uuidString : config.getStringList("spies")) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                spyingPlayers.put(uuid, true);
                String playerName = Bukkit.getOfflinePlayer(uuid).getName();
                if (playerName != null) {
                    playerNameCache.put(uuid, playerName);
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in the config: " + uuidString);
            }
        }

        for (String uuidString : config.getStringList("hiding")) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                hidingPlayers.put(uuid, true);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in the config: " + uuidString);
            }
        }

        for (String uuidString : config.getStringList("ignored")) {
            try {
                UUID uuid = UUID.fromString(uuidString);
                ignoredPlayers.add(uuid);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in the config: " + uuidString);
            }
        }

        dataChanged = false;
    }

    public void saveDataAsync() {
        if (!dataChanged) {
            return;
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::saveDataSync);
    }

    public void saveDataSync() {
        if (!dataChanged) {
            return;
        }

        YamlConfiguration config = new YamlConfiguration();

        config.set("spies", spyingPlayers.keySet().stream()
                .map(UUID::toString)
                .toList());

        config.set("hiding", hidingPlayers.keySet().stream()
                .map(UUID::toString)
                .toList());

        config.set("ignored", ignoredPlayers.stream()
                .map(UUID::toString)
                .toList());

        try {
            config.save(dataFile);
            dataChanged = false;
        } catch (IOException e) {
            plugin.getLogger().warning("Couldn't save data: " + e.getMessage());
        }
    }

    private UUID getPlayerUuid(String playerName) {
        Player player = plugin.getServer().getPlayer(playerName);
        if (player != null) {
            return player.getUniqueId();
        }
        for (Map.Entry<UUID, String> entry : playerNameCache.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(playerName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public String getPlayerName(UUID uuid) {
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            playerNameCache.put(uuid, player.getName());
            return player.getName();
        }
        return playerNameCache.get(uuid);
    }

    public boolean isSpying(UUID uuid) {
        return spyingPlayers.containsKey(uuid);
    }

    public boolean isSpying(String playerName) {
        UUID uuid = getPlayerUuid(playerName);
        return uuid != null && isSpying(uuid);
    }

    public void setSpying(UUID uuid, String playerName, boolean state) {
        if (state) {
            spyingPlayers.put(uuid, true);
            playerNameCache.put(uuid, playerName);
        } else {
            spyingPlayers.remove(uuid);
            playerNameCache.remove(uuid);
        }
        dataChanged = true;
    }

    public boolean isHiding(UUID uuid) {
        return hidingPlayers.containsKey(uuid);
    }

    public boolean isHiding(String playerName) {
        UUID uuid = getPlayerUuid(playerName);
        return uuid != null && isHiding(uuid);
    }

    public void setHiding(UUID uuid, boolean state) {
        if (state) {
            hidingPlayers.put(uuid, true);
        } else {
            hidingPlayers.remove(uuid);
        }
        dataChanged = true;
    }

    public boolean isIgnored(UUID uuid) {
        return ignoredPlayers.contains(uuid);
    }

    public boolean isIgnored(String playerName) {
        UUID uuid = getPlayerUuid(playerName);
        return uuid != null && isIgnored(uuid);
    }

    public boolean addIgnoredPlayer(UUID uuid) {
        boolean result = ignoredPlayers.add(uuid);
        if (result) dataChanged = true;
        return result;
    }

    public boolean removeIgnoredPlayer(UUID uuid) {
        boolean result = ignoredPlayers.remove(uuid);
        if (result) dataChanged = true;
        return result;
    }

    public Set<String> getSpies() {
        Set<String> spies = new HashSet<>();
        for (UUID uuid : spyingPlayers.keySet()) {
            String name = getPlayerName(uuid);
            if (name != null) {
                spies.add(name);
            }
        }
        return spies;
    }

    public Set<UUID> getSpiesUuids() {
        return new HashSet<>(spyingPlayers.keySet());
    }

    public void updatePlayerCache(Player player) {
        playerNameCache.put(player.getUniqueId(), player.getName());
    }
}
