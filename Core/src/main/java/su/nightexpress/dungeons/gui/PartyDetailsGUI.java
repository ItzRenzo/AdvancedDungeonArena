package su.nightexpress.dungeons.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PartyDetailsGUI {

    private static final int SIZE = 27;
    public static final String TITLE = "§8✦ §5Your Party §8✦";

    private static final int LEAVE_SLOT = 11;
    private static final int INFO_SLOT = 13;

    public static void open(Player player) {

        Inventory gui = Bukkit.createInventory(player, SIZE, TITLE);

        gui.setItem(LEAVE_SLOT, createLeaveItem());
        gui.setItem(INFO_SLOT, createInfoItem());

        player.openInventory(gui);
    }

    private static ItemStack createLeaveItem() {

        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§cLeave Party"));

        meta.lore(List.of(
                Component.text("§7Click to leave your party"),
                Component.text("§8(Template action)")
        ));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createInfoItem() {

        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§eParty Information"));

        meta.lore(List.of(
                Component.text("§7Leader: §f(placeholder)"),
                Component.text("§7Members: §f(placeholder)"),
                Component.text(""),
                Component.text("§8This is a template view")
        ));

        item.setItemMeta(meta);
        return item;
    }
}