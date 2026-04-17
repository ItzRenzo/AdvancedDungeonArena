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
import su.nightexpress.dungeons.Components.PartyDetails.KickMemberButton;
import su.nightexpress.dungeons.ComponentUtilities.StaticComponentManager;
import su.nightexpress.dungeons.gui.Utils.GUIConfigManager;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.DungeonPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class KickPlayerGUI {

    public static void open(Player player) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();

        String title = cfg.getString("kick-player.title").replace("&", "§");
        int size = cfg.getInt("kick-player.size");

        Inventory gui = Bukkit.createInventory(player, size, title);

        List<Integer> borderSlots = parseSlots(cfg.get().getStringList("kick-player.slots.border"));
        StaticComponentManager.createBorder(gui, borderSlots.stream().mapToInt(i -> i).toArray());

        Party party = DungeonPlugin.instance.getPartyManager().getPartyByLeader(player.getUniqueId());

        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            player.sendMessage("§cYou are not a party leader.");
            return;
        }

        if (party.getMembers().isEmpty()) {
            player.closeInventory();
            player.sendMessage("§cYour party has no members to kick.");
            return;
        }

        placeMembers(gui, party);

        player.openInventory(gui);
    }

    private static void placeMembers(Inventory gui, Party party) {

        GUIConfigManager cfg = DungeonPlugin.instance.getGUIConfigManager();
        List<Integer> slots = parseSlots(cfg.get().getStringList("kick-player.slots.members"));

        int index = 0;

        for (UUID uuid : party.getMembers()) {

            if (index >= slots.size()) break;

            String name = Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = "Unknown";

            ItemStack head = StaticComponentManager.getPlayerHead(uuid);
            ItemMeta meta = head.getItemMeta();

            if (meta != null) {

                String display = cfg.getString("kick-player.member.name")
                        .replace("&", "§")
                        .replace("%member%", name);

                meta.displayName(Component.text(display));

                NamespacedKey key = new NamespacedKey(DungeonPlugin.instance, "member_uuid");
                meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, uuid.toString());

                List<Component> lore = new ArrayList<>();
                for (String line : cfg.get().getStringList("kick-player.member.lore")) {
                    lore.add(Component.text(line.replace("&", "§").replace("%member%", name)));
                }

                meta.lore(lore);
                head.setItemMeta(meta);
            }

            new KickMemberButton(gui, slots.get(index), head, "kick_member");
            index++;
        }
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