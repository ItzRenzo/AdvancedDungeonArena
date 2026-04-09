package su.nightexpress.dungeons.dungeon.Party;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

// TODO: ADD ADD MEMBER COMMAND
// TODO: ADD DELETE MEMBER COMMAND
// TODO: ADD VIEW MEMBER COMMAND
public class PartyManager {

    private final Map<UUID, PartyRecord> parties = new HashMap<>();

    public void createParty(UUID playerUUID) {
        if (parties.containsKey(playerUUID)) return;
        parties.put(playerUUID, new PartyRecord(playerUUID));
    }

    public void disbandParty(UUID playerUUID) {
        parties.remove(playerUUID);
    }

    public void addMember(UUID leaderUUID, UUID memberUUID) {
        PartyRecord party = parties.get(leaderUUID);
        if (party == null) return;
        party.addMember(memberUUID);
    }

    public void kickMember(UUID leaderUUID, UUID memberUUID) {
        PartyRecord party = parties.get(leaderUUID);
        if (party == null) return;
        party.kickMember(memberUUID);
    }

    public boolean hasParty(UUID playerUUID) {
        return parties.containsKey(playerUUID);
    }

    @Nullable
    public PartyRecord getParty(UUID playerUUID) {
        return parties.get(playerUUID);
    }

    public void sendPartyInfo(UUID playerUUID) {
        Player player = Bukkit.getPlayer(playerUUID);
        if (player == null) return;

        PartyRecord party = getParty(playerUUID);
        if (party == null) {
            player.sendMessage(Component.text("You are not in a party.", NamedTextColor.RED));
            return;
        }

        String leaderName = Bukkit.getOfflinePlayer(party.getPartyLeader()).getName();

        player.sendMessage(Component.empty());

        player.sendMessage(Component.text()
                .append(Component.text("「", NamedTextColor.GOLD))
                .append(Component.text(" PARTY INFO ", NamedTextColor.YELLOW).decorate(TextDecoration.BOLD))
                .append(Component.text("」", NamedTextColor.GOLD))
                .build());

        player.sendMessage(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));

        player.sendMessage(Component.text()
                .append(Component.text("  ★ Leader : ", NamedTextColor.GOLD))
                .append(Component.text(leaderName != null ? leaderName : "Unknown", NamedTextColor.WHITE))
                .build());

        player.sendMessage(Component.text()
                .append(Component.text("  ✦ Members: ", NamedTextColor.AQUA))
                .append(Component.text("(" + party.getPartyMembers().size() + ")", NamedTextColor.GRAY))
                .build());

        if (party.getPartyMembers().isEmpty()) {
            player.sendMessage(Component.text("    • No members yet.", NamedTextColor.GRAY));
        } else {
            for (UUID memberUUID : party.getPartyMembers()) {
                String memberName = Bukkit.getOfflinePlayer(memberUUID).getName();
                boolean isOnline = Bukkit.getPlayer(memberUUID) != null;

                player.sendMessage(Component.text()
                        .append(Component.text("    ● ", isOnline ? NamedTextColor.GREEN : NamedTextColor.RED))
                        .append(Component.text(memberName != null ? memberName : "Unknown", NamedTextColor.WHITE))
                        .append(Component.text(isOnline ? " [Online]" : " [Offline]", NamedTextColor.GRAY))
                        .build());
            }
        }

        player.sendMessage(Component.text("─────────────────────────", NamedTextColor.DARK_GRAY));
        player.sendMessage(Component.empty());
    }
}