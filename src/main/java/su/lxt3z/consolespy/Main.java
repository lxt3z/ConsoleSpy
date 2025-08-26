package su.lxt3z.consolespy;

import su.lxt3z.consolespy.data.PlayerData;
import su.lxt3z.consolespy.utils.CommandSpyManager;
import su.lxt3z.consolespy.utils.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static PlayerData playerData;
    private MessageManager messageManager;
    private LogManager logManager;

    private int autoSaveTaskId;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.messageManager = new MessageManager(this);
        this.logManager = new LogManager(this);
        playerData = new PlayerData(this);
        CommandSpyManager commandSpyManager = new CommandSpyManager(this);

        commandSpyManager.registerCommands();
        commandSpyManager.registerListeners();

        startAutoSaveTask();

        getLogger().info("Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (autoSaveTaskId != 0) {
            getServer().getScheduler().cancelTask(autoSaveTaskId);
        }

        playerData.saveDataSync();
        getLogger().info("Data has been saved!");
    }

    private void startAutoSaveTask() {
        int autoSaveInterval = getConfig().getInt("auto-save-interval") * 20;
        autoSaveTaskId = getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            playerData.saveDataAsync();
            getLogger().info("Auto save task has been started!");
        }, autoSaveInterval, autoSaveInterval).getTaskId();
    }

    public static PlayerData getPlayerData() {
        return playerData;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public LogManager getLogManager() {
        return logManager;
    }
}
