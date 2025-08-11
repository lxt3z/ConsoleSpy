package su.lxt3z.consolespy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import su.lxt3z.consolespy.Main;

public class CommandListener implements Listener {
    private final Main plugin;
    private final PlayerData playerData;
    private final MessageManager messageManager;
    private final LogManager logManager;

    public CommandListener(Main plugin) {
        this.plugin = plugin;
        this.playerData = Main.getPlayerData();
        this.messageManager = plugin.getMessageManager();
        this.logManager = new LogManager(plugin);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().substring(1);
        String playerName = player.getName();

        if (plugin.getConfig().getStringList("ignore-commands").contains(command.split(" ")[0].toLowerCase())) {
            return;
        }

        if (playerData.isIgnored(playerName)) {
            return;
        }

        if (playerData.isHiding(playerName)) {
            return;
        }

        for (String spy : playerData.getSpies()) {
            Player spyPlayer = plugin.getServer().getPlayer(spy);
            if (spyPlayer != null && spyPlayer.isOnline()) {
                spyPlayer.sendMessage(messageManager.getMessage("info-message")
                        .replace("%player%", playerName)
                        .replace("%command%", command));
            }
        }

        logManager.logCommand(playerName, command); // added logManager
    }
    // soon new function
}
