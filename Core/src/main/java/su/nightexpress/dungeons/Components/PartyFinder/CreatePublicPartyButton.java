package su.nightexpress.dungeons.Components.PartyFinder;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;
import su.nightexpress.dungeons.util.CooldownManager;

public class CreatePublicPartyButton extends ComponentButton {

    public CreatePublicPartyButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (e.getCurrentItem() == null) return;


        player.performCommand("ada createopenparty");
        if (DungeonPlugin.instance.getPartyManager().hasParty(player.getUniqueId())) {
            PartyDetailsGUI.open(player);
        }
    }
}