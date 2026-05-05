package su.nightexpress.dungeons.dungeon.reward;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;

import java.util.List;
import java.util.Map;

public class FinishChestListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onChestOpen(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Block block = event.getClickedBlock();
        if (block == null || block.getType() != Material.CHEST) return;

        Chest chestState = (Chest) block.getState();
        String rarity = chestState.getPersistentDataContainer()
                .get(FinishChestRewardManager.getChestKey(), PersistentDataType.STRING);

        if (rarity == null) return;

        event.setCancelled(true);

        String dungeonId = resolveDungeonId(block.getLocation());
        if (dungeonId.isEmpty()) return; // chest not registered to any active dungeon

        // Anti-noclip: player must actually be in this dungeon
        DungeonInstance dungeon = DungeonPlugin.instance.getDungeonManager().getInstanceById(dungeonId);
        if (dungeon == null) return;

        DungeonGamer gamer = DungeonPlugin.instance.getDungeonManager().getDungeonPlayer(event.getPlayer());
        if (gamer == null || gamer.getDungeon() != dungeon) {
            // silently cancel — they shouldn't even see this chest
            return;
        }

        FinishChestRewardManager.onRewardChestOpened(event.getPlayer(), rarity, block.getLocation(), dungeonId);
    }


    private String resolveDungeonId(Location location) {
        for (Map.Entry<String, List<Location>> entry : FinishChestRewardManager.getActiveChests().entrySet()) {
            for (Location chestLoc : entry.getValue()) {
                if (chestLoc.getBlockX() == location.getBlockX()
                        && chestLoc.getBlockY() == location.getBlockY()
                        && chestLoc.getBlockZ() == location.getBlockZ()
                        && chestLoc.getWorld().equals(location.getWorld())) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }
}