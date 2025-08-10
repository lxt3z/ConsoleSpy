package su.lxt3z.consolespy.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import su.lxt3z.consolespy.Main;

public class CommandListener implements Listener {
    private final Main plugin;

    public CommandListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (Main.getPlayerData().isHidden(player.getName())) {
            return;
        }

        String command = event.getMessage().split(" ")[0];
        if (plugin.getConfig().getStringList("ignore-commands").contains(command)) {
            return;
        }

        String message = plugin.getMessageManager().getMessage("info-message")
                .replace("%player%", player.getName())
                .replace("%command%", event.getMessage());

        for (String spyName : Main.getPlayerData().getSpys()) {
            if (spyName.equals(player.getName()) || Main.getPlayerData().isIgnored(spyName, player.getName())) {
                continue;
            }

            Player spy = plugin.getServer().getPlayer(spyName);
            if (spy != null) {
                spy.sendMessage(message);
            }
        }
    }
}