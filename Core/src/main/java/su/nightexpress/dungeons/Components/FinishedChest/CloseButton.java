package su.nightexpress.dungeons.Components.FinishedChest;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;

public class CloseButton extends ComponentButton {

    public CloseButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        player.closeInventory();
    }
}