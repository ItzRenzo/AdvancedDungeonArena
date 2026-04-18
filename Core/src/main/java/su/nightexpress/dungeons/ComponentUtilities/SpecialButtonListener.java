package su.nightexpress.dungeons.ComponentUtilities;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import su.nightexpress.dungeons.util.CooldownManager;

public class SpecialButtonListener implements Listener {

    /**
     * Handles inventory clicks and routes them to registered buttons
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;

        // Check if clicking in the top inventory (the custom GUI)
        if (e.getClickedInventory() == null
                || !e.getClickedInventory().equals(e.getView().getTopInventory())) return;

        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType().isAir()) return;


        // Delegate to ComponentManager
        ComponentManager.handleClick(e);
    }

    /**
     * CRITICAL: Clean up button registrations when inventory closes
     * This prevents memory leaks and ensures buttons don't persist across sessions
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player)) return;

        // Clean up all buttons associated with this inventory
        ComponentManager.unregisterInventory(e.getInventory());
    }
}