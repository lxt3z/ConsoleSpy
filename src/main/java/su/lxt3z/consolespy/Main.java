package su.lxt3z.consolespy;

import su.lxt3z.consolespy.data.PlayerData;
import su.lxt3z.consolespy.utils.CommandSpyManager;
import su.lxt3z.consolespy.utils.MessageManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private static PlayerData playerData;
    private MessageManager messageManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResource("messages.yml", false);

        this.messageManager = new MessageManager(this);
        playerData = new PlayerData(this);
        CommandSpyManager commandSpyManager = new CommandSpyManager(this);

        commandSpyManager.registerCommands();
        commandSpyManager.registerListeners();
    }

    @Override
    public void onDisable() {
        playerData.saveData();
        saveDefaultConfig();
        saveResource("messages.yml", false);
    }

    public static PlayerData getPlayerData() {
        return playerData;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }
}