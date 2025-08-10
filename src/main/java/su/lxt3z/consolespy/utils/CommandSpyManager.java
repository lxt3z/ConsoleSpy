package su.lxt3z.consolespy.utils;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import su.lxt3z.consolespy.Main;
import su.lxt3z.consolespy.listeners.CommandListener;
import su.lxt3z.consolespy.commands.SpyCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommandSpyManager {
    private final Main plugin;

    public CommandSpyManager(Main plugin) {
        this.plugin = plugin;
    }

    public void registerListeners() {
        plugin.getServer().getPluginManager().registerEvents(new CommandListener(plugin), plugin);
        new ChatPacketHandler(plugin).setupChatPacketInterceptor();
    }

    public void registerCommands() {
        Objects.requireNonNull(plugin.getCommand("console")).setExecutor(new SpyCommand(plugin));
        Objects.requireNonNull(plugin.getCommand("console")).setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) {
                return Arrays.asList("on", "off", "hide", "list", "ignore");
            }
            if (args.length == 2 && args[0].equalsIgnoreCase("ignore")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(OfflinePlayer::getName)
                        .collect(Collectors.toList());
            }
            return Collections.emptyList();
        });
    }
}