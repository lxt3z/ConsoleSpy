package su.lxt3z.consolespy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.lxt3z.consolespy.Main;

import java.util.List;

public class SpyCommand implements CommandExecutor {
    private final MessageManager messageManager;
    private final PlayerData playerData;

    public SpyCommand(Main plugin) {
        this.messageManager = plugin.getMessageManager();
        this.playerData = Main.getPlayerData();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("The commands are available only to the players!");
            return true;
        }

        UUID playerUuid = player.getUniqueId();

        if (args.length == 0) {
            player.sendMessage(messageManager.getMessage("help-message"));
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on":
                if (!player.hasPermission("console.use")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                if (playerData.isSpying(playerUuid)) {
                    player.sendMessage(messageManager.getMessage("already-spying"));
                    return true;
                }
                playerData.setSpying(playerUuid, player.getName(), true);
                player.sendMessage(messageManager.getMessage("spy-on"));
                return true;

            case "off":
                if (!player.hasPermission("console.use")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                if (!playerData.isSpying(playerUuid)) {
                    player.sendMessage(messageManager.getMessage("not-spying"));
                    return true;
                }
                playerData.setSpying(playerUuid, player.getName(), false);
                player.sendMessage(messageManager.getMessage("spy-off"));
                return true;

            case "hide":
                if (!player.hasPermission("console.hide")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(messageManager.getMessage("hide-help"));
                    return true;
                }
                String hideArg = args[1].toLowerCase();
                if ("on".equals(hideArg)) {
                    if (playerData.isHiding(playerUuid)) {
                        player.sendMessage(messageManager.getMessage("already-hidden"));
                        return true;
                    }
                    playerData.setHiding(playerUuid, true);
                    player.sendMessage(messageManager.getMessage("hide-on"));
                } else if ("off".equals(hideArg)) {
                    if (!playerData.isHiding(playerUuid)) {
                        player.sendMessage(messageManager.getMessage("not-hidden"));
                        return true;
                    }
                    playerData.setHiding(playerUuid, false);
                    player.sendMessage(messageManager.getMessage("hide-off"));
                } else {
                    player.sendMessage(messageManager.getMessage("hide-help"));
                }
                return true;

            case "list":
                if (!player.hasPermission("console.use")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                player.sendMessage(messageManager.getMessage("list-header"));
                for (String spy : playerData.getSpies()) {
                    player.sendMessage(" - " + spy);
                }
                return true;

            case "ignore":
                if (!player.hasPermission("console.ignore")) {
                    player.sendMessage(messageManager.getMessage("no-permission"));
                    return true;
                }
                if (args.length < 2) {
                    player.sendMessage(messageManager.getMessage("ignore-help"));
                    return true;
                }

                String targetName = args[1];
                Player targetPlayer = Bukkit.getPlayer(targetName);
                UUID targetUuid = null;

                if (targetPlayer != null) {
                    targetUuid = targetPlayer.getUniqueId();
                } else {
                    for (UUID uuid : playerData.getSpiesUuids()) {
                        String name = playerData.getPlayerName(uuid);
                        if (name != null && name.equalsIgnoreCase(targetName)) {
                            targetUuid = uuid;
                            break;
                        }
                    }
                }

                if (targetUuid == null) {
                    player.sendMessage(messageManager.getMessage("player-not-found").replace("%player%", targetName));
                    return true;
                }

                if (args.length >= 3 && "off".equalsIgnoreCase(args[1])) {
                    targetName = args[2];
                    if (playerData.removeIgnoredPlayer(targetUuid)) {
                        player.sendMessage(messageManager.getMessage("player-unignored").replace("%player%", targetName));
                    } else {
                        player.sendMessage(messageManager.getMessage("player-not-ignored").replace("%player%", targetName));
                    }
                } else {
                    if (playerData.addIgnoredPlayer(targetUuid)) {
                        player.sendMessage(messageManager.getMessage("player-ignored").replace("%player%", targetName));
                    } else {
                        player.sendMessage(messageManager.getMessage("player-already-ignored").replace("%player%", targetName));
                    }
                }
                return true;

            default:
                player.sendMessage(messageManager.getMessage("unknown-command"));
                return true;
        }
    }
}
