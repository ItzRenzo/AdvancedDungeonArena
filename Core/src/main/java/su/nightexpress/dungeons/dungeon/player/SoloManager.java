package su.nightexpress.dungeons.dungeon.player;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SoloManager {

    Set<UUID> soloPlayers = new HashSet<>();


    public boolean isSolo(UUID playerId) {
        return soloPlayers.contains(playerId);
    }

    public void makePlayerSoloOptionOn(UUID playerId) {
        soloPlayers.add(playerId);
    }

    public void makePlayerSoloOptionOff(UUID playerId) {
        soloPlayers.remove(playerId);
    }

    public void toggleSoloOption(UUID playerId) {
        if (isSolo(playerId)) {
            makePlayerSoloOptionOff(playerId);
        }
        else {
            makePlayerSoloOptionOn(playerId);
        }
    }

    public void clearOnLeave(UUID playerId) {
        makePlayerSoloOptionOff(playerId);
    }

    public void checkPlayerTempSettings(Player player) {
        if (isSolo(player.getUniqueId())) {
            player.sendMessage("Your solo option is on. You can turn it off with /soloMode");
        }
        else {
            player.sendMessage("Your solo option is off. You can turn it on with /soloMode");
        }
    }
}
