package su.nightexpress.dungeons.gui.GuiListener;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;

public class PartyDetailsListener implements Listener {


    @EventHandler
    public void onClick(InventoryClickEvent e) {

        if (!(e.getWhoClicked() instanceof Player player)) return;

        if (e.getClickedInventory() == null) return;

        if (!e.getView().title().equals(net.kyori.adventure.text.Component.text(PartyDetailsGUI.TITLE))) return;

        e.setCancelled(true);

        if (e.getCurrentItem() == null || e.getCurrentItem().getType() == Material.AIR) return;

        ItemStack item = e.getCurrentItem();
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return;

        // Placeholder click behavior for now
        player.sendMessage("§aYou clicked a party entry.");

    }
}