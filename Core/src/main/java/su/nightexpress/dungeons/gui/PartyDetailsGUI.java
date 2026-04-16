package su.nightexpress.dungeons.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.dungeons.ComponentUtilities.StaticComponentManager;
import su.nightexpress.dungeons.Components.PartyDetails.LeavePartyButton;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.List;
import java.util.UUID;

public class PartyDetailsGUI {

    private static final int SIZE = 45;
    public static final String TITLE = "Party Details";

    private static final int[] BORDER = {
            0,1,2,3,4,5,6,7,8,
            9,17,
            18,26,
            27,35,
            36,37,38,39,40,41,42,43,44
    };

    private static final int INFO_SLOT = 4;
    private static final int LEAVE_SLOT = 40;

    private static final int[] MEMBER_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34
    };

    public static void open(Player player) {

        Inventory gui = Bukkit.createInventory(player, SIZE, TITLE);

        StaticComponentManager.createBorder(gui, BORDER);

        Party party = null;
        UUID uuid = player.getUniqueId();

        for (Party p : DungeonPlugin.instance.getPartyManager().getAllParties()) {
            if (p.getLeader().equals(uuid) || p.getMembers().contains(uuid)) {
                party = p;
                break;
            }
        }

        if (party == null) {
            player.closeInventory();
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        gui.setItem(INFO_SLOT, createLeaderHead(party));
        new LeavePartyButton(
                gui,
                LEAVE_SLOT,
                createLeaveItem(),
                "leave_party"
        );

        placeMembers(gui, party);

        player.openInventory(gui);
    }

    private static ItemStack createLeaderHead(Party party) {

        String leaderName = Bukkit.getOfflinePlayer(party.getLeader()).getName();
        if (leaderName == null) leaderName = "Unknown";

        ItemStack item = StaticComponentManager.getPlayerHead(leaderName);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            meta.displayName(Component.text("§6👑 " + leaderName));

            meta.lore(List.of(
                    Component.text("§eParty Leader"),
                    Component.text("§7Has full control over the party"),
                    Component.text(""),
                    Component.text("§7Members: §f" + party.getMembers().size())
            ));

            item.setItemMeta(meta);
        }

        return item;
    }

    private static void placeMembers(Inventory gui, Party party) {

        int index = 0;

        for (UUID uuid : party.getMembers()) {

            if (index >= MEMBER_SLOTS.length) break;

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = "Unknown";

            ItemStack head = StaticComponentManager.getPlayerHead(name);
            ItemMeta meta = head.getItemMeta();

            if (meta != null) {

                meta.displayName(Component.text("§f" + name));

                meta.lore(List.of(
                        Component.text("§7Party Member"),
                        Component.text("§8In this party")
                ));

                head.setItemMeta(meta);
            }

            gui.setItem(MEMBER_SLOTS[index], head);
            index++;
        }
    }

    private static ItemStack createLeaveItem() {

        ItemStack item = new ItemStack(Material.BARRIER);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§cLeave Party"));

        meta.lore(List.of(
                Component.text("§7Leave your current party"),
                Component.text(""),
                Component.text("§eClick to leave")
        ));

        item.setItemMeta(meta);
        return item;
    }
}