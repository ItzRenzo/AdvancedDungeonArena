package su.nightexpress.dungeons.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import su.nightexpress.dungeons.ComponentUtilities.StaticComponentManager;
import su.nightexpress.dungeons.Components.PartyDetails.GoToKickMembersButton;
import su.nightexpress.dungeons.Components.PartyDetails.LeavePartyButton;
import su.nightexpress.dungeons.Components.PartyDetails.ReturnButton;
import su.nightexpress.dungeons.Components.PartyDetails.TogglePartyVisibilityButton;
import su.nightexpress.dungeons.gui.Utils.GUIConfigManager;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PartyDetailsGUI {

    public static void open(Player player) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        String title = cfg.getString("party-details.title").replace("&", "§");
        int size = cfg.getInt("party-details.size");

        Inventory gui = Bukkit.createInventory(player, size, title);

        List<Integer> borderSlots = parseSlots(cfg.get().getStringList("party-details.slots.border"));
        StaticComponentManager.createBorder(gui, borderSlots.stream().mapToInt(i -> i).toArray());

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

        int leaderSlot = cfg.getInt("party-details.slots.leader");
        int leaveSlot = cfg.getInt("party-details.slots.leave");
        int kickSlot   = cfg.getInt("party-details.slots.kick");
        int returnSlot = cfg.getInt("party-details.slots.return");



        gui.setItem(leaderSlot, createLeaderHead(party));

        new ReturnButton(
                gui,
                returnSlot,
                createReturnItem(),
                "return"
        );

        new LeavePartyButton(
                gui,
                leaveSlot,
                createLeaveItem(),
                "leave_party"
        );

        if (party.getLeader().equals(uuid)) {
            new GoToKickMembersButton(gui, kickSlot, createKickItem(), "go_to_kick_members");

            new TogglePartyVisibilityButton(
                    gui,
                    cfg.getInt("party-details.slots.toggle"),
                    createToggleItem(party),
                    "toggle_party_visibility"
            );
        }

        placeMembers(gui, party);

        player.openInventory(gui);
    }

    private static ItemStack createLeaderHead(Party party) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        String leaderName = Bukkit.getOfflinePlayer(party.getLeader()).getName();
        UUID leaderUUid =  Bukkit.getOfflinePlayer(party.getLeader()).getUniqueId();

        if (leaderName == null) leaderName = "Unknown";

        ItemStack item = StaticComponentManager.getPlayerHead(leaderUUid);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            String name = cfg.getString("party-details.leader.name")
                    .replace("&", "§")
                    .replace("%leader%", leaderName);

            meta.displayName(Component.text(name));

            List<Component> lore = new ArrayList<>();
            for (String line : cfg.get().getStringList("party-details.leader.lore")) {
                lore.add(Component.text(
                        line.replace("&", "§")
                                .replace("%members%", String.valueOf(party.getMembers().size()))
                ));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static void placeMembers(Inventory gui, Party party) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        List<Integer> slots = parseSlots(cfg.get().getStringList("party-details.slots.members"));

        int index = 0;

        for (UUID uuid : party.getMembers()) {

            if (index >= slots.size()) break;

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = "Unknown";

            ItemStack head = StaticComponentManager.getPlayerHead(uuid);
            ItemMeta meta = head.getItemMeta();

            if (meta != null) {

                String display = cfg.getString("party-details.member.name")
                        .replace("&", "§")
                        .replace("%member%", name);

                meta.displayName(Component.text(display));

                List<Component> lore = new ArrayList<>();
                for (String line : cfg.get().getStringList("party-details.member.lore")) {
                    lore.add(Component.text(line.replace("&", "§")));
                }

                meta.lore(lore);
                head.setItemMeta(meta);
            }

            gui.setItem(slots.get(index), head);
            index++;
        }
    }

    private static ItemStack createLeaveItem() {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-details.buttons.leave";

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

    private static ItemStack createKickItem() {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-details.buttons.kick";

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

    private static ItemStack createToggleItem(Party party) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        String path = "party-details.buttons.toggle";

        String state = party.isOpen() ? "public" : "private";

        ItemStack item = new ItemStack(Material.valueOf(cfg.getString(path + "." + state + ".material")));
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {

            String name = cfg.getString(path + "." + state + ".name")
                    .replace("&", "§");

            meta.displayName(Component.text(name));

            List<Component> lore = new ArrayList<>();
            for (String line : cfg.get().getStringList(path + "." + state + ".lore")) {
                lore.add(Component.text(line.replace("&", "§")));
            }

            meta.lore(lore);
            item.setItemMeta(meta);
        }

        return item;
    }

    private static ItemStack createReturnItem() {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        String path = "party-details.buttons.return";

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