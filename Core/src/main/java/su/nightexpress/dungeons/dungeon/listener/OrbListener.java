package su.nightexpress.dungeons.dungeon.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.kit.OrbManager;

import java.util.List;

public class OrbListener implements Listener {

    private final DungeonPlugin plugin;
    private final OrbManager orbManager;

    public OrbListener(DungeonPlugin plugin, OrbManager orbManager) {
        this.plugin = plugin;
        this.orbManager = orbManager;
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (orbManager.isOrb(event.getItemInHand())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!orbManager.isOrb(item)) return;

        event.setCancelled(true);

        if (!plugin.getDungeonManager().isPlaying(player)) {
            player.sendMessage("§cYou can only use orbs while in a raid.");
            return;
        }

        String visualClass = plugin.getClassManager().getClass(player);

        if (visualClass == null || visualClass.isEmpty()) {
            player.sendMessage("§cNo class assigned.");
            return;
        }

        List<String> commands = orbManager.getOrbCommands(item, visualClass);

        for (String cmd : commands) {
            String parsed = cmd.replace("%player%", player.getName());
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), parsed);
        }
    }
}