package su.nightexpress.dungeons.dungeon.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import su.nightexpress.dungeons.dungeon.Party.Party;
import su.nightexpress.dungeons.kit.impl.Kit;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record QueueEntry(List<Player> players, Kit kit) {
    public static QueueEntry ofPlayer(Player player, Kit kit) {
        List<Player> list = new ArrayList<>();
        list.add(player);
        return new QueueEntry(list, kit);
    }

    public static QueueEntry ofParty(Party party, Kit kit) {
        List<Player> members = party.getAllMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(p -> p != null)
                .collect(Collectors.toList());
        return new QueueEntry(members, kit);
    }

    public boolean isSolo() {
        return players.size() == 1;
    }

    public Player firstPlayer() {
        return players.get(0);
    }
}
