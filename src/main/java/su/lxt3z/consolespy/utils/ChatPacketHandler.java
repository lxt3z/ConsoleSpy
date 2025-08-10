package su.lxt3z.consolespy.utils;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import su.lxt3z.consolespy.Main;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatPacketHandler {
    private final Main plugin;
    private final ProtocolManager protocolManager;
    private final Map<UUID, CommandTracker> commandTrackers = new ConcurrentHashMap<>();

    public ChatPacketHandler(Main plugin) {
        this.plugin = plugin;
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    public void setupChatPacketInterceptor() {
        protocolManager.addPacketListener(new PacketAdapter(
                plugin,
                ListenerPriority.NORMAL,
                PacketType.Play.Server.CHAT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                Player receiver = event.getPlayer();
                if (!Main.getPlayerData().isSpying(receiver.getName())) return;

                PacketContainer packet = event.getPacket();
                WrappedChatComponent component = packet.getChatComponents().read(0);
                String message = component.getJson();

                if (!message.contains("\"%player%\"") && !message.contains("\"%command%\"")) return;

                UUID receiverId = receiver.getUniqueId();
                CommandTracker tracker = commandTrackers.computeIfAbsent(receiverId, k -> new CommandTracker());
                String processedMessage = tracker.processMessage(message);

                if (processedMessage != null) {
                    packet.getChatComponents().write(0, WrappedChatComponent.fromJson(processedMessage));
                } else {
                    event.setCancelled(true);
                }
            }
        });
    }

    private static class CommandTracker {
        private String lastMessage;
        private int repeatCount;
        private long lastTimestamp;

        private static final long REPEAT_WINDOW = 3000;

        public String processMessage(String message) {
            long currentTime = System.currentTimeMillis();

            if (isSameCommand(message, currentTime)) {
                repeatCount++;
                return updateMessageWithCount(message);
            } else {
                String result = lastMessage != null ? updateMessageWithCount(lastMessage) : null;
                lastMessage = message;
                repeatCount = 1;
                lastTimestamp = currentTime;
                return result != null ? result : message;
            }
        }

        private boolean isSameCommand(String message, long currentTime) {
            return message.equals(lastMessage) &&
                    (currentTime - lastTimestamp) <= REPEAT_WINDOW;
        }

        private String updateMessageWithCount(String message) {
            if (repeatCount <= 1) return message;
            return message.replace("}", ",\"extra\":[{\"text\":\" (x" + repeatCount + ")\"}]}");
        }
    }
}
