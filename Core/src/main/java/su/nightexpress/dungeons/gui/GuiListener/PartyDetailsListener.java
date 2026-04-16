package su.nightexpress.dungeons.gui.GuiListener;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;

public class PartyDetailsListener implements Listener {

    @EventHandler
    public void onAnyClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player)) return;
        if (e.getClickedInventory() != e.getView().getTopInventory()) return;

        Component title = e.getView().title();

        if (!title.equals(Component.text(PartyDetailsGUI.TITLE))) return;

        e.setCancelled(true);
    }
}