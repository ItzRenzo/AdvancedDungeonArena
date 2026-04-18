package su.nightexpress.dungeons.Components.KickPlayer;

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
import su.nightexpress.dungeons.gui.KickPlayerGUI;

import java.util.UUID;

public class KickMemberButton extends ComponentButton {

    public KickMemberButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        NamespacedKey key = new NamespacedKey(DungeonPlugin.instance, "member_uuid");

        String uuidString = meta.getPersistentDataContainer().get(
                key,
                PersistentDataType.STRING
        );

        if (uuidString == null) {
            player.sendMessage("§cInvalid member data.");
            return;
        }

        UUID targetUUID = UUID.fromString(uuidString);

        PartyManager partyManager = DungeonPlugin.instance.getPartyManager();

        if (!partyManager.hasParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        if (partyManager.getPartyByLeader(player.getUniqueId()) == null) {
            player.sendMessage("§cOnly the party leader can kick members.");
            return;
        }

        String targetName = player.getServer().getOfflinePlayer(targetUUID).getName();
        if (targetName == null) targetName = targetUUID.toString();

        partyManager.kickMember(player.getUniqueId(), targetUUID);
        player.sendMessage("§aYou kicked §f" + targetName + " §afrom the party.");
        KickPlayerGUI.open(player);
    }
}