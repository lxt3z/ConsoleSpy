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

    private final ThreadLocal<SimpleDateFormat> dateFormat;

    private final ExecutorService logExecutor;

    public LogManager(Main plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.enabled = config.getBoolean("logging.enabled");
        this.logPath = config.getString("logging.path");
        this.logFormat = config.getString("logging.format");
        this.maxSizeBytes = config.getInt("logging.max-size-mb") * 1024 * 1024;
        this.rotate = config.getBoolean("logging.rotate");

        assert logFormat != null;
        String datePattern = logFormat.contains("%date%") ?
                extractDateFormat() : "yyyy-MM-dd HH:mm:ss";
        this.dateFormat = ThreadLocal.withInitial(() -> new SimpleDateFormat(datePattern));

        this.logExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "ConsoleSpy-LogWriter");
            thread.setDaemon(true);
            return thread;
        });

        assert logPath != null;
        File logFile = new File(logPath);
        if (!logFile.getParentFile().exists()) {
            logFile.getParentFile().mkdirs();
        }
    }

    private String extractDateFormat() {
        return "yyyy-MM-dd HH:mm:ss";
    }

    public void logCommand(String playerName, String command) {
        if (!enabled) return;

        logExecutor.submit(() -> {
            String timestamp = dateFormat.get().format(new Date());
            String logEntry = logFormat
                    .replace("%date%", timestamp)
                    .replace("%player%", playerName)
                    .replace("%command%", command);

            File logFile = new File(logPath);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write(logEntry);
                writer.newLine();

                if (rotate && logFile.exists() && logFile.length() > maxSizeBytes) {
                    rotateLogFile(logFile);
                }

            } catch (IOException e) {
                plugin.getLogger().warning("Couldn't write to the log: " + e.getMessage());
            }
        });
    }

    private void rotateLogFile(File logFile) {
        try {
            String backupPath = logPath.replace(".log",
                    "_" + new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()) + ".log");
            File backupFile = new File(backupPath);

            if (logFile.renameTo(backupFile)) {
                plugin.getLogger().info("The log file has been renamed: " + backupFile.getName());
            } else {
                plugin.getLogger().warning("Couldn't rename the log file");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error when rotating the log file: " + e.getMessage());
        }
    }

    public void shutdown() {
        logExecutor.shutdown();
    }
}
