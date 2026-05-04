package su.nightexpress.dungeons.dungeon.reward;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.DungeonsAPI;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FinishChestRewardManager {

    private static NamespacedKey chestKey;
    private static final Map<String, List<Entity>> activeDisplays = new HashMap<>();
    private static final Map<String, List<Location>> activeChests = new HashMap<>();

    // --- Init / Teardown ---

    public static void init(@NotNull Plugin plugin) {
        chestKey = new NamespacedKey(plugin, "finish_chest_rarity");
    }

    public static void shutdown() {
        cleanupAll();
        chestKey = null;
    }

    // --- Spawn ---

    public static void spawnRewardChests(@NotNull DungeonInstance dungeon) {
        BlockPos pos = dungeon.getConfig().getFinishChestPos();
        if (chestKey == null) {
            chestKey = new NamespacedKey(DungeonsAPI.getPlugin(), "finish_chest_rarity");
        }

        if (pos == null) {
            DungeonsAPI.getPlugin().getLogger().warning(
                    "Finish chest position is null for dungeon: " + dungeon.getConfig().getId());
            return;
        }

        String dungeonId = dungeon.getConfig().getId();

        if (hasActiveChests(dungeon)) {
            DungeonsAPI.getPlugin().getLogger().warning(
                    "Reward chests already active for dungeon: " + dungeonId + ", skipping spawn.");
            return;
        }

        World world = dungeon.getWorld();
        Location center = pos.toLocation(world);

        int[] offsets      = {-2, 0, 2};
        String[] rarities  = {"COMMON", "RARE", "LEGENDARY"};
        TextColor[] colors = {
                TextColor.color(0xAAAAAA),  // Common    - gray
                TextColor.color(0x5555FF),  // Rare      - blue
                TextColor.color(0xFFAA00)   // Legendary - gold
        };

        List<Entity> displays = new ArrayList<>();
        List<Location> chests = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            Location chestLoc = center.clone().add(offsets[i], 0, 0);

            chestLoc.getBlock().setType(Material.CHEST);

            Chest chestState = (Chest) chestLoc.getBlock().getState();
            chestState.getPersistentDataContainer().set(
                    chestKey,
                    PersistentDataType.STRING,
                    rarities[i]
            );
            chestState.update();

            chests.add(chestLoc);

            Location holoLoc = chestLoc.clone().add(0.5, 1.5, 0.5);
            TextDisplay display = (TextDisplay) world.spawnEntity(holoLoc, EntityType.TEXT_DISPLAY);

            Component label = Component.text(rarities[i])
                    .color(colors[i])
                    .decoration(TextDecoration.BOLD, true);

            display.text(label);
            display.setBillboard(Display.Billboard.CENTER);
            display.setAlignment(TextDisplay.TextAlignment.CENTER);
            display.setShadowed(true);
            display.setPersistent(false);
            display.setViewRange(24f);

            displays.add(display);
        }

        activeDisplays.put(dungeonId, displays);
        activeChests.put(dungeonId, chests);

        DungeonsAPI.getPlugin().getLogger().info(
                "Spawned reward chests for dungeon: " + dungeonId);
    }

    // --- Reward ---

    public static void onRewardChestOpened(@NotNull Player player, @NotNull String rarity, @NotNull Location location) {
        switch (rarity) {
            case "COMMON" -> player.sendMessage(Component.text("You opened a Common chest!")
                    .color(TextColor.color(0xAAAAAA)));
            case "RARE" -> player.sendMessage(Component.text("You opened a Rare chest!")
                    .color(TextColor.color(0x5555FF)));
            case "LEGENDARY" -> player.sendMessage(Component.text("You opened a Legendary chest!")
                    .color(TextColor.color(0xFFAA00)));
            default -> DungeonsAPI.getPlugin().getLogger().warning(
                    "Unknown rarity on chest open: " + rarity);
        }
    }

    // --- Cleanup ---

    public static void cleanupRewardChests(@NotNull DungeonInstance dungeon) {
        String dungeonId = dungeon.getConfig().getId();

        List<Entity> displays = activeDisplays.remove(dungeonId);
        if (displays != null) displays.forEach(Entity::remove);

        List<Location> chests = activeChests.remove(dungeonId);
        if (chests != null) chests.forEach(loc -> loc.getBlock().setType(Material.AIR));

        DungeonsAPI.getPlugin().getLogger().info(
                "Cleaned up reward chests for dungeon: " + dungeonId);
    }

    public static void cleanupAll() {
        activeDisplays.values().forEach(list -> list.forEach(Entity::remove));
        activeDisplays.clear();

        activeChests.values().forEach(list ->
                list.forEach(loc -> loc.getBlock().setType(Material.AIR)));
        activeChests.clear();
    }

    // --- Util ---

    public static boolean hasActiveChests(@NotNull DungeonInstance dungeon) {
        return activeDisplays.containsKey(dungeon.getConfig().getId());
    }

    public static NamespacedKey getChestKey() {
        return chestKey;
    }
}