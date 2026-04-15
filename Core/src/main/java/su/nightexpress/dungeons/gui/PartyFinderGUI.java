package su.nightexpress.dungeons.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.dungeons.ComponentUtilities.StaticComponentManager;
import su.nightexpress.dungeons.Components.JoinPartyButton;
import su.nightexpress.dungeons.Components.CreatePartyButton;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.List;
import java.util.UUID;

public class PartyFinderGUI {

    public static final int SIZE = 45;
    public static final String TITLE = "§8✦ §5Party Finder §8✦";

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

    private static final int CREATE_SLOT = 4;

    public static void createGUI(Player player) {

        Inventory gui = Bukkit.createInventory(player, SIZE, TITLE);

        StaticComponentManager.createBorder(gui, BORDER);

        new CreatePartyButton(
                gui,
                CREATE_SLOT,
                createCreatePartyItem(),
                null
        );

        int index = 0;

        for (Party party : DungeonPlugin.instance.getPartyManager().getAllParties()) {

            if (index >= PARTY_SLOTS.length) break;

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

    private static ItemStack createCreatePartyItem() {

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

    private static ItemStack createPartyItem(Party party) {

        ItemStack item = new ItemStack(Material.CAMPFIRE);
        ItemMeta meta = item.getItemMeta();

        if (meta == null) return item;

        UUID leaderId = party.getLeader();

        String leaderName = Bukkit.getPlayer(leaderId) != null
                ? Bukkit.getPlayer(leaderId).getName()
                : "Unknown";

        meta.displayName(Component.text("§eParty of §f" + leaderName));

        meta.lore(List.of(
                Component.text("§7Members: §f" + party.getMembers().size()),
                Component.text(""),
                Component.text("§aClick to join")
        ));

        item.setItemMeta(meta);
        return item;
    }
}