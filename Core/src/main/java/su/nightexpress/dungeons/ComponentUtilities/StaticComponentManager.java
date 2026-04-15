package su.nightexpress.dungeons.ComponentUtilities;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ResolvableProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.intellij.lang.annotations.Subst;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"unused", "DuplicatedCode"})
public class StaticComponentManager {

    public static ItemStack createItem(Material mat, @Nullable Component name, @Nullable Component lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(name);
            }
            if (lore != null) {
                meta.lore(wrapLoreLine(lore));
            }
            item.setItemMeta(meta);
        }
        return item;
    }





    public static ItemStack createItem(Material mat, @Nullable Component name, @Nullable Component lore, @Nullable String username) {
        if (mat != Material.PLAYER_HEAD) {
            return createItem(mat, name, lore);
        }

        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            if (name != null) {
                meta.displayName(name);
            }
            if (lore != null) {
                meta.lore(wrapLoreLine(lore));
            }
            if (username != null && !username.isEmpty()) {
                meta.setOwningPlayer(Bukkit.getOfflinePlayer(username));
            }
            head.setItemMeta(meta);
        }
        return head;
    }

    public static List<Component> wrapLoreLine(Component input) {
        List<Component> result = new ArrayList<>();
        if (input == null) return result;

        String plainText = PlainTextComponentSerializer.plainText().serialize(input);
        Style baseStyle = input.style();

        if (baseStyle.color() == null) {
            baseStyle = baseStyle.color(NamedTextColor.GRAY);
        }

        String[] words = plainText.split(" ");
        StringBuilder line = new StringBuilder();

        for (String word : words) {
            if (line.length() + word.length() + 1 > 30) {
                result.add(Component.text(line.toString(), baseStyle));
                line = new StringBuilder();
            }
            if (!line.isEmpty()) line.append(" ");
            line.append(word);
        }
        if (!line.isEmpty()) {
            result.add(Component.text(line.toString(), baseStyle));
        }
        return result;
    }

    public static void createBorder(Inventory gui, int[] borderSlots) {
        ItemStack border = StaticComponentManager.createItem(Material.BLACK_STAINED_GLASS_PANE,
                Component.empty(), Component.empty());
        for (int slot : borderSlots) {
            gui.setItem(slot, border);
        }
    }

    public static ItemStack createEmptyNamedBlackPane() {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            item.setItemMeta(meta);
        }
        return item;
    }

    @SuppressWarnings("UnstableApiUsage")
    public static ItemStack getPlayerHead(@Subst("") String name) {
        var skull = ItemStack.of(Material.PLAYER_HEAD);
        skull.setData(DataComponentTypes.PROFILE,
                ResolvableProfile.resolvableProfile().name(name));
        return skull;
    }





}