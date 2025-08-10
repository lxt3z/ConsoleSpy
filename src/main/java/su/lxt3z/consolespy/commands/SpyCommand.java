package su.lxt3z.consolespy.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import su.lxt3z.consolespy.Main;

import java.util.List;

public class SpyCommand implements CommandExecutor {
    private final Main plugin;

    public SpyCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player) || !cmd.getName().equalsIgnoreCase("console")) {
            return false;
        }

        if (args.length == 0) {
            player.sendMessage(getMessage("help-message"));
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "on":
                handleSpyOn(player);
                break;
            case "off":
                handleSpyOff(player);
                break;
            case "hide":
                handleHide(player, args);
                break;
            case "list":
                handleList(player);
                break;
            case "ignore":
                handleIgnore(player, args);
                break;
            default:
                player.sendMessage(getMessage("unknown-command"));
        }
        return true;
    }

    private void handleSpyOn(Player player) {
        if (!player.hasPermission("console.use")) {
            noPerm(player);
            return;
        }
        if (!Main.getPlayerData().isSpying(player.getName())) {
            Main.getPlayerData().addSpy(player.getName());
            player.sendMessage(getMessage("spy-on"));
        } else {
            player.sendMessage(getMessage("already-spying"));
        }
    }

    private void handleSpyOff(Player player) {
        if (!player.hasPermission("console.use")) {
            noPerm(player);
            return;
        }
        if (Main.getPlayerData().isSpying(player.getName())) {
            Main.getPlayerData().removeSpy(player.getName());
            player.sendMessage(getMessage("spy-off"));
        } else {
            player.sendMessage(getMessage("not-spying"));
        }
    }

    private void handleHide(Player player, String[] args) {
        if (!player.hasPermission("console.hide")) {
            noPerm(player);
            return;
        }
        if (args.length < 2) {
            player.sendMessage(getMessage("hide-help"));
            return;
        }
        String state = args[1].toLowerCase();
        if (state.equals("on")) {
            if (!Main.getPlayerData().isHidden(player.getName())) {
                Main.getPlayerData().addHidden(player.getName());
                player.sendMessage(getMessage("hide-on"));
            } else {
                player.sendMessage(getMessage("already-hidden"));
            }
        } else if (state.equals("off")) {
            if (Main.getPlayerData().isHidden(player.getName())) {
                Main.getPlayerData().removeHidden(player.getName());
                player.sendMessage(getMessage("hide-off"));
            } else {
                player.sendMessage(getMessage("not-hidden"));
            }
        } else {
            player.sendMessage(getMessage("hide-help"));
        }
    }

    private void handleList(Player player) {
        List<String> spys = Main.getPlayerData().getSpys();
        String list = String.join(", ", spys);
        player.sendMessage(getMessage("list-header") + " " + list);
    }

    private void handleIgnore(Player player, String[] args) {
        if (!player.hasPermission("console.ignore")) {
            noPerm(player);
            return;
        }
        if (args.length < 2) {
            player.sendMessage(getMessage("ignore-help"));
            return;
        }
        String target = args[1];
        if (target.equalsIgnoreCase("off")) {
            if (args.length < 3) {
                player.sendMessage(getMessage("ignore-off-help"));
                return;
            }
            String targetOff = args[2];
            if (Main.getPlayerData().isIgnored(player.getName(), targetOff)) {
                Main.getPlayerData().unignore(player.getName(), targetOff);
                player.sendMessage(getMessage("player-unignored").replace("%player%", targetOff));
            } else {
                player.sendMessage(getMessage("player-not-ignored").replace("%player%", targetOff));
            }
        } else {
            if (Main.getPlayerData().isIgnored(player.getName(), target)) {
                player.sendMessage(getMessage("player-already-ignored").replace("%player%", target));
            } else {
                Main.getPlayerData().ignore(player.getName(), target);
                player.sendMessage(getMessage("player-ignored").replace("%player%", target));
            }
        }
    }

    private String getMessage(String key) {
        return plugin.getMessageManager().getMessage(key);
    }

    private void noPerm(Player player) {
        player.sendMessage(getMessage("no-permission"));
    }
}