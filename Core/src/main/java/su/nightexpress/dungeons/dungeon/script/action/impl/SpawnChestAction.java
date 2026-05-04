package su.nightexpress.dungeons.dungeon.script.action.impl;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import su.nightexpress.dungeons.DungeonsAPI;
import su.nightexpress.dungeons.dungeon.game.DungeonInstance;
import su.nightexpress.dungeons.dungeon.event.game.DungeonGameEvent;
import su.nightexpress.dungeons.dungeon.reward.FinishChestRewardManager;
import su.nightexpress.dungeons.dungeon.script.action.Action;
import su.nightexpress.dungeons.dungeon.script.action.ActionId;
import su.nightexpress.dungeons.util.ErrorHandler;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;

public class SpawnChestAction implements Action {

    public static final String ACTION_ID = "spawn_chest";

    @NotNull
    public static SpawnChestAction load(@NotNull FileConfig config, @NotNull String path) {
        return new SpawnChestAction();
    }

    @Override
    public void write(@NotNull FileConfig config, @NotNull String path) {
        // No data to write
    }

    @NotNull
    @Override
    public String getName() {
        return ActionId.SPAWN_CHEST;
    }

    @Override
    public void perform(@NotNull DungeonInstance dungeon, @NotNull DungeonGameEvent event) {
        DungeonsAPI.getPlugin().getLogger().info(
                "SpawnChestAction.perform called for dungeon: " + dungeon.getConfig().getId());
        FinishChestRewardManager.spawnRewardChests(dungeon);
    }
}