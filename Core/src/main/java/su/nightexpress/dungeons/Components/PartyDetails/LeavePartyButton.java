package su.nightexpress.dungeons.Components.PartyDetails;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.gui.PartyFinderGUI;

public class LeavePartyButton extends ComponentButton {

    public LeavePartyButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        player.performCommand("ada partyleave");

        PartyFinderGUI.open(player);
    }
}