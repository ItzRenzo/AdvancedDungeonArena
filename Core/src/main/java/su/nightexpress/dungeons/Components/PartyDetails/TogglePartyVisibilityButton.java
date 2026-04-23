package su.nightexpress.dungeons.Components.PartyDetails;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;

public class TogglePartyVisibilityButton extends ComponentButton {

    public TogglePartyVisibilityButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        DungeonPlugin.instance
                .getPartyManager()
                .togglePartyVisibility(player.getUniqueId());

        // Refresh GUI so item updates visually
        PartyDetailsGUI.open(player);
    }
}