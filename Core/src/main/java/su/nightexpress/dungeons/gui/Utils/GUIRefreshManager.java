package su.nightexpress.dungeons.gui.Utils;

import org.bukkit.entity.Player;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.gui.KickPlayerGUI;
import su.nightexpress.dungeons.gui.PartyDetailsGUI;
import su.nightexpress.dungeons.gui.PartyFinderGUI;
import su.nightexpress.dungeons.gui.ReadyCheckGUI;

public class GUIRefreshManager  {

    public static void open(Player player, String id) {

        switch (id.toLowerCase()) {

            case "party-finder" -> PartyFinderGUI.open(player);

            case "party-details" -> PartyDetailsGUI.open(player);

            case "kick-player" -> KickPlayerGUI.open(player);

            case "ready" -> ReadyCheckGUI.open(player);

            default -> player.sendMessage("§cUnknown GUI: " + id);
        }
    }

    public static void refresh(Player player, String id) {
        open(player, id); // simple rebuild
    }
}