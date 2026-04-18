package su.nightexpress.dungeons.util;

import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CooldownManager {

    private static final int COOLDOWN_MS = 3000;
    private static final Map<UUID, Long> COOLDOWNS = new ConcurrentHashMap<>();

    // true = is on cooldown, false = can proceed
    public static boolean isOnCooldown(Player player) {
        long now = System.currentTimeMillis();
        long lastUsed = COOLDOWNS.getOrDefault(player.getUniqueId(), 0L);
        long elapsed = now - lastUsed;

        if (elapsed < COOLDOWN_MS) {
            long remaining = (COOLDOWN_MS - elapsed + 999) / 1000;
            player.sendMessage("§cPlease wait §e" + remaining + "s §cbefore doing that again.");
            return true;
        }

        COOLDOWNS.put(player.getUniqueId(), now);
        return false;
    }

    public static void clearCooldown(Player player) {
        COOLDOWNS.remove(player.getUniqueId());
    }

    public static void clearAll() {
        COOLDOWNS.clear();
    }
}