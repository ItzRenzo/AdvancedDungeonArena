package su.nightexpress.dungeons.Components.ReadyCheck;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.dungeon.Party.PartyManager;
import su.nightexpress.dungeons.gui.ReadyCheckGUI;

public class ReadyToggleButton extends ComponentButton {

    public ReadyToggleButton(org.bukkit.inventory.Inventory inv, int slot, org.bukkit.inventory.ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        PartyManager partyManager = DungeonPlugin.instance.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        player.performCommand("ada ready");

        ReadyCheckGUI.open(player);
    }
}