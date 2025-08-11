package su.lxt3z.consolespy.utils;

import su.lxt3z.consolespy.Main;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogManager {
    private final Main plugin;
    private final String logPath;
    private final String logFormat;
    private final int maxSizeBytes;
    private final boolean rotate;
    private final boolean enabled;

    public LogManager(Main plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("logging.enabled");
        this.logPath = config.getString("logging.path");
        this.logFormat = config.getString("logging.format");
        this.maxSizeBytes = config.getInt("logging.max-size-mb") * 1024 * 1024;
        this.rotate = config.getBoolean("logging.rotate");
    }

    public void logCommand(String playerName, String command) {
        if (!enabled) return;

        SimpleDateFormat dateFormat = new SimpleDateFormat(logFormat.contains("%date%") ? logFormat.replace("%date%", "yyyy-MM-dd HH:mm:ss") : "yyyy-MM-dd HH:mm:ss");
        String timestamp = dateFormat.format(new Date());
        String logEntry = logFormat
                .replace("%date%", timestamp)
                .replace("%player%", playerName)
                .replace("%command%", command);

        File logFile = new File(logPath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write(logEntry);
            writer.newLine();

            if (rotate && logFile.length() > maxSizeBytes) {
                String backupPath = logPath.replace(".txt", "_backup_" + System.currentTimeMillis() + ".txt");
                logFile.renameTo(new File(backupPath));
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Couldn't log in: " + e.getMessage());
        }
    }
}
