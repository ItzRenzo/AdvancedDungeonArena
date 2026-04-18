package su.nightexpress.dungeons.Components;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import su.nightexpress.dungeons.ComponentUtilities.ComponentButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.gui.Utils.GUIRefreshManager;

public class RefreshButton extends ComponentButton {

    private static final NamespacedKey GUI_KEY =
            new NamespacedKey(DungeonPlugin.instance, "gui_id");

    public RefreshButton(org.bukkit.inventory.Inventory inv, int slot, ItemStack item, String baseKey) {
        super(inv, slot, item, baseKey);
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        e.setCancelled(true);

        if (!(e.getWhoClicked() instanceof Player player)) return;

        ItemStack item = e.getCurrentItem();
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        String guiId = meta.getPersistentDataContainer().get(
                GUI_KEY,
                PersistentDataType.STRING
        );

        if (guiId == null) {
            player.sendMessage("§cThis GUI cannot be refreshed.");
            return;
        }

        // Dispatch refresh based on ID
        GUIRefreshManager.refresh(player, guiId);
    }
}