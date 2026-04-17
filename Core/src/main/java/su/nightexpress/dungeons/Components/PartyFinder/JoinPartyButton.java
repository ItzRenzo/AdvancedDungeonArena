package su.nightexpress.dungeons.Components.PartyFinder;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.Party.PartyManager;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;

import java.util.UUID;

public class JoinPartyButton extends ComponentButton {

    public JoinPartyButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey(DungeonPlugin.instance, "party_leader");

        String uuidString = meta.getPersistentDataContainer().get(
                key,
                PersistentDataType.STRING
        );

        if (uuidString == null) {
            player.sendMessage("§cInvalid party data.");
            return;
        }

        UUID uuid = UUID.fromString(uuidString);

        String leaderName = player.getServer().getOfflinePlayer(uuid).getName();

        if (leaderName == null) {
            player.sendMessage("§cLeader not found.");
            return;
        }

        player.performCommand("ada joinparty " + leaderName);

        if (DungeonPlugin.instance.getPartyManager().getPartyOf(player.getUniqueId()) != null) {
            PartyDetailsGUI.open(player);
        }

    }

}