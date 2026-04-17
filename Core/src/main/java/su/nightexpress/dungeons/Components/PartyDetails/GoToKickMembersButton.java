package su.nightexpress.dungeons.Components.PartyDetails;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.gui.KickPlayerGUI;

import java.util.UUID;

public class GoToKickMembersButton extends ComponentButton {

    public GoToKickMembersButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {

        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        UUID uuid = player.getUniqueId();
        Party party = null;

        for (Party p : DungeonPlugin.instance.getPartyManager().getAllParties()) {
            if (p.getLeader().equals(uuid) || p.getMembers().contains(uuid)) {
                party = p;
                break;
            }
        }

        if (party == null) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        if (party.getMembers().isEmpty()) {
            player.sendMessage("§cThere are no members to kick.");
            return;
        }


        KickPlayerGUI.open(player);
    }
}