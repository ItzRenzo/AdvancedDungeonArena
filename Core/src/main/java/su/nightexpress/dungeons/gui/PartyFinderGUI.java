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
import su.nightexpress.dungeons.Components.PartyFinder.JoinPartyButton;
import su.nightexpress.dungeons.Components.PartyFinder.CreatePublicPartyButton;
import su.nightexpress.dungeons.Components.PartyFinder.CreatePrivatePartyButton;
import su.nightexpress.dungeons.Components.PartyFinder.ViewPartyDetailsButton;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.List;
import java.util.UUID;

public class PartyFinderGUI {

    public static final int SIZE = 45;
    public static final String TITLE = "Party Finder";

    private static final int[] BORDER = {
            0,1,2,3,4,5,6,7,8,
            9,17,
            18,26,
            27,35,
            36,37,38,39,40,41,42,43,44
    };

    private static final int[] PARTY_SLOTS = {
            10,11,12,13,14,15,16,
            19,20,21,22,23,24,25,
            28,29,30,31,32,33,34
    };

    private static final int PUBLIC_PARTY_SLOT = 2;
    private static final int PRIVATE_PARTY_SLOT = 6;
    private static final int VIEW_PARTY_SLOT = 4;

    public static void createGUI(Player player) {

        Inventory gui = Bukkit.createInventory(player, SIZE, TITLE);

        StaticComponentManager.createBorder(gui, BORDER);

        UUID uuid = player.getUniqueId();

        Party playerParty = null;

        for (Party party : DungeonPlugin.instance.getPartyManager().getAllParties()) {

            if (party.getLeader().equals(uuid) || party.getMembers().contains(uuid)) {
                playerParty = party;
                break;
            }
        }

        if (playerParty != null) {

            new ViewPartyDetailsButton(
                    gui,
                    VIEW_PARTY_SLOT,
                    createPartyDetailsItem(playerParty),
                    null
            );

        } else {

            new CreatePublicPartyButton(
                    gui,
                    PUBLIC_PARTY_SLOT,
                    createPublicPartyItemStack(),
                    null
            );


            new CreatePrivatePartyButton(
                    gui,
                    PRIVATE_PARTY_SLOT,
                    createPrivatePartyItemStack(),
                    null
            );
        }

        int index = 0;

        for (Party party : DungeonPlugin.instance.getPartyManager().getAllParties()) {

            if (index >= PARTY_SLOTS.length) break;

            if (!party.isOpen()) { continue; }

            ItemStack item = createPartyItem(party);

            new JoinPartyButton(
                    gui,
                    PARTY_SLOTS[index],
                    item,
                    null
            );

            index++;
        }

        player.openInventory(gui);
    }

    private static ItemStack createPublicPartyItemStack() {

        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§aCreate Party"));

        meta.lore(List.of(
                Component.text("§7Start your own party"),
                Component.text(""),
                Component.text("§eClick to create")
        ));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPrivatePartyItemStack() {

        ItemStack item = new ItemStack(Material.EMERALD_BLOCK);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§dCreate Private Party"));

        meta.lore(List.of(
                Component.text("§7Start a private party"),
                Component.text("§7Invite-only access"),
                Component.text(""),
                Component.text("§eClick to create")
        ));

        item.setItemMeta(meta);
        return item;
    }




    private static ItemStack createPartyDetailsItem(Party party) {

        ItemStack item = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text("§bYour Party"));

        meta.lore(List.of(
                Component.text("§7Members: §f" + party.getMembers().size()),
                Component.text(""),
                Component.text("§eClick to view details")
        ));

        item.setItemMeta(meta);
        return item;
    }

    private static ItemStack createPartyItem(Party party) {

        UUID leaderId = party.getLeader();

        String leaderName = Bukkit.getOfflinePlayer(leaderId).getName();
        if (leaderName == null) leaderName = "Unknown";

        ItemStack item = StaticComponentManager.getPlayerHead(leaderName);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            meta.displayName(Component.text("§eParty of §f" + leaderName));

            meta.lore(List.of(
                    Component.text("§7Members: §f" + party.getMembers().size()),
                    Component.text(""),
                    Component.text("§aClick to join")
            ));

            meta.getPersistentDataContainer().set(
                    new NamespacedKey(DungeonPlugin.instance, "party_leader"),
                    PersistentDataType.STRING,
                    leaderId.toString()
            );

            item.setItemMeta(meta);
        }

        return item;
    }

}