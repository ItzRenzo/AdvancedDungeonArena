package su.nightexpress.dungeons.ComponentUtilities;

import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ComponentManager {
    // Thread-safe map: uniqueKey -> ComponentButton instance
    private static final Map<String, ComponentButton> BUTTON_REGISTRY = new ConcurrentHashMap<>();

    // Thread-safe map: Inventory UUID -> Set of button keys in that inventory
    private static final Map<UUID, Set<String>> INVENTORY_BUTTONS = new ConcurrentHashMap<>();

    // Map to track inventory UUIDs (since Inventory doesn't have a built-in UUID)
    private static final Map<Inventory, UUID> INVENTORY_IDS = new ConcurrentHashMap<>();

    /**
     * Registers a button instance
     */
    public static void registerButton(ComponentButton button) {
        String key = button.getUniqueKey();
        Inventory inv = button.getInventory();

        // Register the button
        BUTTON_REGISTRY.put(key, button);

        // Associate button with inventory
        UUID invId = getOrCreateInventoryId(inv);
        INVENTORY_BUTTONS.computeIfAbsent(invId, k -> ConcurrentHashMap.newKeySet()).add(key);

        // Debug logging
        //Bukkit.getLogger().info("[ComponentManager] Registered button: " + key + " for inventory: " + invId);
    }

    /**
     * Gets or creates a UUID for an inventory
     */
    private static UUID getOrCreateInventoryId(Inventory inv) {
        return INVENTORY_IDS.computeIfAbsent(inv, k -> UUID.randomUUID());
    }

    /**
     * Handles click events by routing to the appropriate button
     */
    public static void handleClick(InventoryClickEvent e) {
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        // Get the unique key from PDC
        NamespacedKey pdcKey = new NamespacedKey(DungeonPlugin.instance, "component_key");
        String uniqueKey = meta.getPersistentDataContainer().get(pdcKey, PersistentDataType.STRING);

        if (uniqueKey == null) {
            return;
        }

        // Find and trigger the button
        ComponentButton button = BUTTON_REGISTRY.get(uniqueKey);
        if (button != null) {
            // Verify the button belongs to the clicked inventory
            if (button.getInventory().equals(e.getClickedInventory())) {
                button.onClick(e);
            }
        }
    }

    /**
     * Cleans up all buttons associated with an inventory
     * IMPORTANT: Call this when an inventory is closed!
     */
    public static void unregisterInventory(Inventory inv) {
        UUID invId = INVENTORY_IDS.remove(inv);
        if (invId == null) {
            return;
        }

        Set<String> buttonKeys = INVENTORY_BUTTONS.remove(invId);
        if (buttonKeys != null) {
            for (String key : buttonKeys) {
                ComponentButton button = BUTTON_REGISTRY.remove(key);
                if (button != null) {
                    button.onDestroy();
                }
            }
            //Bukkit.getLogger().info("[ComponentManager] Unregistered " + buttonKeys.size() + " buttons for inventory: " + invId);
        }
    }

    /**
     * Gets a button by its unique key (for debugging)
     */
    public static ComponentButton getButton(String uniqueKey) {
        return BUTTON_REGISTRY.get(uniqueKey);
    }

    /**
     * Gets all registered button keys (for debugging)
     */
    public static Set<String> getRegisteredKeys() {
        return new HashSet<>(BUTTON_REGISTRY.keySet());
    }

    /**
     * Gets the number of registered buttons (for debugging)
     */
    public static int getButtonCount() {
        return BUTTON_REGISTRY.size();
    }

    /**
     * Gets the number of active inventories (for debugging)
     */
    public static int getInventoryCount() {
        return INVENTORY_IDS.size();
    }

    /**
     * Force cleanup of all registered components (use for plugin reload)
     */
    public static void cleanupAll() {
        for (ComponentButton button : BUTTON_REGISTRY.values()) {
            button.onDestroy();
        }
        BUTTON_REGISTRY.clear();
        INVENTORY_BUTTONS.clear();
        INVENTORY_IDS.clear();
        //Bukkit.getLogger().info("[ComponentManager] All components cleaned up");
    }
}