package su.nightexpress.dungeons.dungeon.reward;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.player.DungeonGamer;

public class ChestLockManager {

    public static boolean ifOpeningChestAllowed(Player player, String dungeonId) {
        // Anti-noclip: player must actually be in this dungeon
        DungeonInstance dungeon = DungeonPlugin.instance.getDungeonManager().getInstanceById(dungeonId);
        if (dungeon == null) return false;

        DungeonGamer gamer = DungeonPlugin.instance.getDungeonManager().getDungeonPlayer(player);
        // silently cancel — they shouldn't even see this chest
        return gamer != null && gamer.getDungeon() == dungeon;
    }

    public static void lockPlayerChestInteraction(Player player, String dungeonId) {
        FinishChestRewardManager.playersSpecificRewardInventory.put(
                player.getUniqueId(),
                Bukkit.createInventory(player, 27, Component.text("Dungeon Reward - " + dungeonId))
        );
    }

    public static void unlockPlayerChestInteraction(Player player) {
        FinishChestRewardManager.playersSpecificRewardInventory.remove(player.getUniqueId());
    }



    public static boolean ifPlayerAlreadyOpenedADifferentRewardChest(Player player) {
        return FinishChestRewardManager.playersSpecificRewardInventory.containsKey(player.getUniqueId());
    }

}
