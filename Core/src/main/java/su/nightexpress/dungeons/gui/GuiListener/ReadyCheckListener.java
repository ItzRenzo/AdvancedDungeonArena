package su.nightexpress.dungeons.gui.GuiListener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import su.nightexpress.dungeons.DungeonPlugin;

public class ReadyCheckListener implements Listener {

    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        String title = e.getView().getTitle();

        String expected = DungeonPlugin.instance.getGUIConfigManager()
                .getString("ready.title")
                .replace("&", "§");

        if (!title.equals(expected)) return;

        e.setCancelled(true);
    }
}