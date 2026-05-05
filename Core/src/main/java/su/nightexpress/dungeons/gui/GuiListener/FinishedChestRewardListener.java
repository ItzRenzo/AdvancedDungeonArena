package su.nightexpress.dungeons.gui.GuiListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.List;

public class FinishedChestRewardListener implements Listener {

    private static final List<String> RARITIES = List.of("common", "rare", "legendary");

    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        String title = e.getView().getTitle();

        boolean isFinishedChest = RARITIES.stream().anyMatch(rarity -> {
            String raw = DungeonPlugin.instance.getGUIConfigManager()
                    .getString("finished-chest.title." + rarity);
            return raw != null && title.equals(raw.replace("&", "§"));
        });

        if (!isFinishedChest) return;

        e.setCancelled(true);
    }
}