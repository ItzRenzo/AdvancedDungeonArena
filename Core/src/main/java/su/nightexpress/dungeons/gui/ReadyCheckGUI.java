package su.nightexpress.dungeons.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.dungeons.ComponentUtilities.StaticComponentManager;
import su.nightexpress.dungeons.Components.ReadyCheck.ReadyToggleButton;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.gui.Utils.GUIConfigManager;

import java.util.*;

public class ReadyCheckGUI {

    public static void open(Player player) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        String title = cfg.getString("ready.title").replace("&", "§");
        int size = cfg.getInt("ready.size");

        Inventory gui = Bukkit.createInventory(player, size, title);

        Party party = DungeonPlugin.instance.getPartyManager().getPartyOf(player.getUniqueId());

        if (party == null) {
            player.closeInventory();
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        // Border
        List<Integer> borderSlots = parseSlots(cfg.get().getStringList("ready.slots.border"));
        StaticComponentManager.createBorder(gui, borderSlots.stream().mapToInt(i -> i).toArray());

        placeMembers(gui, party, cfg);
        placeReadyButton(gui, player, cfg);
        placeStatus(gui, party, cfg);

        player.openInventory(gui);
    }

    private static void placeMembers(Inventory gui, Party party, GUIConfigManager cfg) {

        List<Integer> slots = parseSlots(cfg.get().getStringList("ready.slots.members"));
        Set<UUID> ready = DungeonPlugin.instance.getPartyManager().getReadyPlayers();

        int index = 0;

        for (UUID uuid : party.getAllMembers()) {

            if (index >= slots.size()) break;

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = "Unknown";

            boolean isReady = ready.contains(uuid);

            ItemStack head = StaticComponentManager.getPlayerHead(uuid);
            ItemMeta meta = head.getItemMeta();

            if (meta != null) {

                String icon = isReady ? "§a●" : "§c●";
                String status = isReady ? "§aREADY" : "§cNOT READY";

                meta.displayName(Component.text(icon + " §f" + name));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.text(" "));
                lore.add(Component.text("§7Status: " + status));
                lore.add(Component.text(" "));
                lore.add(Component.text(isReady ? "§aReady for queue" : "§cNot ready"));

                meta.lore(lore);

                if (isReady) {
                    meta.setEnchantmentGlintOverride(true);
                }

                head.setItemMeta(meta);
            }

            gui.setItem(slots.get(index), head);
            index++;
        }
    }

    private static void placeReadyButton(Inventory gui, Player player, GUIConfigManager cfg) {

        boolean ready = DungeonPlugin.instance.getPartyManager()
                .getReadyPlayers()
                .contains(player.getUniqueId());

        String path = ready ? "ready.button.ready" : "ready.button.not-ready";

        Material material = Material.valueOf(cfg.getString(path + ".material"));
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));

            List<Component> lore = new ArrayList<>();
            for (String line : cfg.get().getStringList(path + ".lore")) {
                lore.add(Component.text(line.replace("&", "§")));
            }

            meta.lore(lore);

            item.setItemMeta(meta);
        }

        int slot = cfg.getInt("ready.slots.ready-button");

        new ReadyToggleButton(gui, slot, item, "ready_toggle");
    }

    private static void placeStatus(Inventory gui, Party party, GUIConfigManager cfg) {

        Set<UUID> ready = DungeonPlugin.instance.getPartyManager().getReadyPlayers();
        boolean allReady = party.getAllMembers().stream().allMatch(ready::contains);

        String path = allReady ? "ready.status.ready" : "ready.status.waiting";

        Material material = Material.valueOf(cfg.getString(path + ".material"));
        ItemStack item = new ItemStack(material);

        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));
            item.setItemMeta(meta);
        }

        int slot = cfg.getInt("ready.slots.status");
        gui.setItem(slot, item);
    }

    private static List<Integer> parseSlots(List<String> list) {
        List<Integer> slots = new ArrayList<>();
        for (String s : list) {
            for (String part : s.split(",")) {
                slots.add(Integer.parseInt(part.trim()));
            }
        }
        return slots;
    }
}