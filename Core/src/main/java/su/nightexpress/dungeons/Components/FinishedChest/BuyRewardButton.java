package su.nightexpress.dungeons.Components.FinishedChest;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.dungeon.reward.FinishedChestRewardGui;

public class BuyRewardButton extends ComponentButton {

    public BuyRewardButton(Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        // Purchase logic will go here
        // e.g. DungeonPlugin.instance.getRewardManager().purchase(player);
    }
}