package su.nightexpress.dungeons.Components;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;

public class MyPartyButton extends ComponentButton {

    public MyPartyButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;

        player.sendMessage("§e[Party] Opening your party...");

        PartyDetailsGUI.open(player);
    }
}