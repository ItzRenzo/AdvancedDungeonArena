package su.nightexpress.dungeons.Components;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;

public class JoinPartyButton extends ComponentButton {

    public JoinPartyButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        player.performCommand("ada party join");

        player.sendMessage("§a[Party] Attempting to join...");
    }
}