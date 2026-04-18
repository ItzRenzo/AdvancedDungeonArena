package su.nightexpress.dungeons.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import su.nightexpress.dungeons.ComponentUtilities.StaticComponentManager;
import su.nightexpress.dungeons.Components.PartyFinder.*;
import su.nightexpress.dungeons.Components.RefreshButton;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.gui.Utils.GUIConfigManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyFinderGUI {

    public static void open(Player player) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        String title = cfg.getString("party-finder.title").replace("&", "§");
        int size = cfg.getInt("party-finder.size");

        Inventory gui = Bukkit.createInventory(player, size, title);

        List<Integer> borderSlots = parseSlots(cfg.get().getStringList("party-finder.slots.border"));
        StaticComponentManager.createBorder(gui, borderSlots.stream().mapToInt(i -> i).toArray());

        int refreshSlot = cfg.getInt("party-finder.buttons.refresh.slot");
        new RefreshButton(
                gui,
                refreshSlot,
                createRefreshItem(),
                null
        );

        UUID uuid = player.getUniqueId();

        Party playerParty = null;

        for (Party party : DungeonPlugin.instance.getPartyManager().getAllParties()) {
            if (party.getLeader().equals(uuid) || party.getMembers().contains(uuid)) {
                playerParty = party;
                break;
            }
        }

        if (playerParty != null) {

            int slot = cfg.getInt("party-finder.buttons.view-party.slot");

            new ViewPartyDetailsButton(
                    gui,
                    slot,
                    createPartyDetailsItem(playerParty),
                    "ViewPartyDetailsButton"
            );

            int readySlot = cfg.getInt("party-finder.buttons.ready-check.slot");
            new ReadyCheckButton(
                    gui,
                    readySlot,
                    createReadyCheckItem(),
                    "ReadyCheckButton"
            );

        } else {

            int publicSlot = cfg.getInt("party-finder.buttons.public-party.slot");
            int privateSlot = cfg.getInt("party-finder.buttons.private-party.slot");

            new CreatePublicPartyButton(
                    gui,
                    publicSlot,
                    createPublicPartyItemStack(),
                    "CreatePublicPartyButton"
            );

            new CreatePrivatePartyButton(
                    gui,
                    privateSlot,
                    createPrivatePartyItemStack(),
                    "CreatePrivatePartyButton"
            );
        }

        List<Integer> partySlots = parseSlots(cfg.get().getStringList("party-finder.slots.parties"));

        int index = 0;

        for (Party party : DungeonPlugin.instance.getPartyManager().getAllParties()) {

            if (index >= partySlots.size()) break;
            if (!party.isOpen()) continue;

            ItemStack item = createPartyItem(party);

            new JoinPartyButton(
                    gui,
                    partySlots.get(index),
                    item,
                    "joinPartyButton"
            );

            index++;
        }

        player.openInventory(gui);
    }

    private static ItemStack createPublicPartyItemStack() {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-finder.buttons.public-party";

        ItemStack item = new ItemStack(Material.valueOf(cfg.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));

        List<Component> lore = new ArrayList<>();
        for (String line : cfg.get().getStringList(path + ".lore")) {
            lore.add(Component.text(line.replace("&", "§")));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPrivatePartyItemStack() {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-finder.buttons.private-party";

        ItemStack item = new ItemStack(Material.valueOf(cfg.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));

        List<Component> lore = new ArrayList<>();
        for (String line : cfg.get().getStringList(path + ".lore")) {
            lore.add(Component.text(line.replace("&", "§")));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPartyDetailsItem(Party party) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-finder.buttons.view-party";

        ItemStack item = new ItemStack(Material.valueOf(cfg.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));

        List<Component> lore = new ArrayList<>();
        for (String line : cfg.get().getStringList(path + ".lore")) {
            lore.add(Component.text(
                    line.replace("&", "§")
                            .replace("%members%", String.valueOf(party.getMembers().size()))
            ));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPartyItem(Party party) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        UUID leaderId = party.getLeader();
        String leaderName = Bukkit.getOfflinePlayer(leaderId).getName();
        UUID leaderID = Bukkit.getOfflinePlayer(leaderId).getUniqueId();
        if (leaderName == null) leaderName = "Unknown";

        ItemStack item = StaticComponentManager.getPlayerHead(leaderID);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            String name = cfg.getString("party-finder.party-item.name")
                    .replace("&", "§")
                    .replace("%leader%", leaderName);

            meta.displayName(Component.text(name));

            List<Component> lore = new ArrayList<>();
            for (String line : cfg.get().getStringList("party-finder.party-item.lore")) {
                lore.add(Component.text(
                        line.replace("&", "§")
                                .replace("%members%", String.valueOf(party.getMembers().size()))
                ));
            }

            meta.lore(lore);

            meta.getPersistentDataContainer().set(
                    new NamespacedKey(DungeonPlugin.instance, "party_leader"),
                    PersistentDataType.STRING,
                    leaderId.toString()
            );

            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createRefreshItem() {
        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-finder.buttons.refresh";

        ItemStack item = new ItemStack(Material.valueOf(cfg.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));

        List<Component> lore = new ArrayList<>();
        for (String line : cfg.get().getStringList(path + ".lore")) {
            lore.add(Component.text(line.replace("&", "§")));
        }

        meta.lore(lore);

        meta.getPersistentDataContainer().set(
                new NamespacedKey(DungeonPlugin.instance, "gui_id"),
                PersistentDataType.STRING,
                "party-finder"
        );

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createReadyCheckItem() {
        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-finder.buttons.ready-check";

        ItemStack item = new ItemStack(Material.valueOf(cfg.getString(path + ".material")));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(cfg.getString(path + ".name").replace("&", "§")));

        List<Component> lore = new ArrayList<>();
        for (String line : cfg.get().getStringList(path + ".lore")) {
            lore.add(Component.text(line.replace("&", "§")));
        }

        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
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