package su.nightexpress.dungeons.ComponentUtilities;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import su.nightexpress.dungeons.DungeonPlugin;

import javax.annotation.Nullable;
import java.util.*;

@SuppressWarnings({"unused", "DuplicatedCode"})
public abstract class ComponentButton {
    protected ItemStack item;
    protected int slot;
    protected Inventory inv;
    protected final String uniqueKey;

    /**
     * Creates a new ComponentButton with a unique key per instance
     *
     * @param inv The inventory to add the button to
     * @param slot The slot position
     * @param material The material type
     * @param name Display name component
     * @param baseKey Base key for identification (will be made unique)
     * @param lore Optional lore lines
     */
    public ComponentButton(Inventory inv, int slot, Material material, Component name, String baseKey, @Nullable String playerHeadName, @Nullable Component... lore) {
        if (playerHeadName != null) {
            this.item = StaticComponentManager.getPlayerHead(playerHeadName);
        } else {
            this.item = new ItemStack(material);
        }


        this.slot = slot;
        this.inv = inv;
        // Create unique key using UUID to prevent collisions
        this.uniqueKey = baseKey + "_" + UUID.randomUUID().toString();

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(name);

            if (lore != null && lore.length > 0) {
                List<Component> loreList = new ArrayList<>();
                for (Component line : lore) {
                    if (line != null) {
                        loreList.add(line);
                    }
                }
                meta.lore(loreList);
            }

            // Store the unique key in PDC
            NamespacedKey pdcKey = new NamespacedKey(DungeonPlugin.instance, "component_key");
            meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, uniqueKey);

            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);

        // Register this button instance
        ComponentManager.registerButton(this);
    }

    public ComponentButton(Inventory inv, int slot, ItemStack existingItem, String baseKey) {
        this.item = existingItem.clone();
        this.slot = slot;
        this.inv = inv;
        this.uniqueKey = baseKey + "_" + UUID.randomUUID();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            NamespacedKey pdcKey = new NamespacedKey(DungeonPlugin.instance, "component_key");
            meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, uniqueKey);
            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
        ComponentManager.registerButton(this);
    }

    public ComponentButton(Inventory inv, int slot, Material material, int amount,
                           Component name, String baseKey, @Nullable String playerHeadName,
                           @Nullable Component... lore) {

        // Create item stack (head or material)
        if (playerHeadName != null) {
            this.item = StaticComponentManager.getPlayerHead(playerHeadName);
        } else {
            this.item = new ItemStack(material, amount);
        }

        this.slot = slot;
        this.inv = inv;

        this.uniqueKey = baseKey + "_" + UUID.randomUUID();

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(name);

            if (lore != null && lore.length > 0) {
                meta.lore(Arrays.stream(lore).filter(Objects::nonNull).toList());
            }

            NamespacedKey pdcKey = new NamespacedKey(DungeonPlugin.instance, "component_key");
            meta.getPersistentDataContainer().set(pdcKey, PersistentDataType.STRING, uniqueKey);

            item.setItemMeta(meta);
        }

        inv.setItem(slot, item);
        ComponentManager.registerButton(this);
    }

    private List<Component> wrapText(Component text) {
        List<Component> result = new ArrayList<>();

        // Get the plain text content
        String plainText = PlainTextComponentSerializer.plainText().serialize(text);

        // Get the base style from the original component
        Style baseStyle = text.style();

        StringBuilder line = new StringBuilder();

        for (String word : plainText.split(" ")) {
            if (line.length() + word.length() + 1 > 30) {
                result.add(Component.text(line.toString().trim(), baseStyle));
                line = new StringBuilder();
            }
            line.append(word).append(" ");
        }

        if (!line.toString().isBlank()) {
            result.add(Component.text(line.toString().trim(), baseStyle));
        }

        return result;
    }

    public ItemStack getItem() {
        return this.item;
    }

    public void changeItemStackDetails(@Nullable Material material,
                                       @Nullable Component name,
                                       @Nullable List<Component> lore,
                                       @Nullable Map<Enchantment, Integer> enchantments) {

        if (item == null) return;

        // Change material if provided
        if (material != null) {
            ItemStack newItem = new ItemStack(material, item.getAmount());
            ItemMeta oldMeta = item.getItemMeta();
            if (oldMeta != null) {
                newItem.setItemMeta(oldMeta);
            }
            item = newItem;
        }

        // Get or create ItemMeta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        // Change display name if provided
        if (name != null) {
            meta.displayName(name);
        }

        // Change lore if provided
        if (lore != null) {
            meta.lore(lore);
        }

        // Remove all old enchantments
        for (Enchantment e : item.getEnchantments().keySet()) {
            meta.removeEnchant(e);
        }

        // Add new enchantments if provided
        if (enchantments != null) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                meta.addEnchant(entry.getKey(), entry.getValue(), true);
            }
        }

        // Apply meta
        item.setItemMeta(meta);

        // Update the inventory slot
        if (inv != null) {
            inv.setItem(slot, item);
        }
    }

    /**
     * Gets the unique key for this button instance
     */
    public String getUniqueKey() {
        return uniqueKey;
    }

    /**
     * Gets the inventory this button belongs to
     */
    public Inventory getInventory() {
        return inv;
    }

    /**
     * Called when this button is clicked
     */
    public abstract void onClick(InventoryClickEvent e);

    /**
     * Called when the button should clean up resources
     * Override this if your button needs cleanup
     */
    public void onDestroy() {
        // Default: no cleanup needed
    }
}