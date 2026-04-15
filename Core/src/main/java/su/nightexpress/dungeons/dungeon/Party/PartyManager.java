package su.nightexpress.dungeons.dungeon.Party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class PartyManager {

    private final Map<UUID, Party> partyByLeader;
    private final Map<UUID, UUID> memberToLeader;
    private final Set<UUID> readyPlayers;
    private final Map<UUID, String> pendingQueueRequest;


    public PartyManager() {
        this.partyByLeader = new HashMap<>();
        this.memberToLeader = new HashMap<>();
        this.readyPlayers = new HashSet<>();
        this.pendingQueueRequest = new HashMap<>();
    }

    public void createParty(@NotNull UUID leaderId) {
        Party party = new Party(leaderId);
        this.partyByLeader.put(leaderId, party);
        this.memberToLeader.put(leaderId, leaderId);
    }

    public void disbandParty(@NotNull UUID leaderId) {
        Party party = this.partyByLeader.remove(leaderId);
        if (party == null) return;

        this.pendingQueueRequest.remove(leaderId);

        for (UUID memberId : party.getAllMembers()) {
            this.memberToLeader.remove(memberId);
            this.readyPlayers.remove(memberId);
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) {
                member.sendMessage("§cYour party has been disbanded.");
            }
        }
    }

    public void invitePlayer(@NotNull UUID leaderId, @NotNull UUID targetId) {
        Party party = this.getPartyByLeader(leaderId);
        if (party == null) return;

        party.addInvite(targetId);

        Player target = Bukkit.getPlayer(targetId);
        Player leader = Bukkit.getPlayer(leaderId);
        if (target != null && leader != null) {
            target.sendMessage("§aYou have been invited to §f" + leader.getName() + "§a's party! Type §f/dungeon accept " + leader.getName() + " §aor §f/dungeon decline " + leader.getName() + "§a.");
        }
    }

    public void acceptInvite(@NotNull UUID playerId, @NotNull UUID leaderId) {
        Party party = this.partyByLeader.get(leaderId);
        if (party == null || !party.hasPendingInvite(playerId)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.sendMessage("§cNo pending invite from that player.");
            return;
        }

        party.removeInvite(playerId);
        party.addMember(playerId);
        this.memberToLeader.put(playerId, party.getLeader());

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) player.sendMessage("§aYou joined the party!");

        broadcastToParty(party, "§a" + (player != null ? player.getName() : playerId) + " joined the party!");
    }

    public void declineInvite(@NotNull UUID playerId, @NotNull UUID leaderId) {
        Party party = this.partyByLeader.get(leaderId);
        if (party == null || !party.hasPendingInvite(playerId)) {
            Player player = Bukkit.getPlayer(playerId);
            if (player != null) player.sendMessage("§cNo pending invite from that player.");
            return;
        }

        party.removeInvite(playerId);

        Player player = Bukkit.getPlayer(playerId);
        Player leader = Bukkit.getPlayer(leaderId);

        if (player != null) player.sendMessage("§cYou declined the party invite.");
        if (leader != null) leader.sendMessage("§c" + (player != null ? player.getName() : "A player") + " declined your invite.");
    }


    public void leaveParty(@NotNull UUID playerId) {
        UUID leaderId = this.memberToLeader.get(playerId);
        if (leaderId == null) return;

        Party party = this.partyByLeader.get(leaderId);
        if (party == null) return;

        if (party.isLeader(playerId)) {
            disbandParty(playerId);
            return;
        }

        party.removeMember(playerId);
        this.memberToLeader.remove(playerId);
        this.readyPlayers.remove(playerId);

        Player player = Bukkit.getPlayer(playerId);
        if (player != null) player.sendMessage("§cYou left the party.");
        broadcastToParty(party, "§c" + (player != null ? player.getName() : playerId) + " left the party.");
    }

    public void kickMember(@NotNull UUID leaderId, @NotNull UUID targetId) {
        Party party = this.getPartyByLeader(leaderId);
        if (party == null) return;

        party.removeMember(targetId);
        this.memberToLeader.remove(targetId);
        this.readyPlayers.remove(targetId);

        Player target = Bukkit.getPlayer(targetId);
        if (target != null) target.sendMessage("§cYou were kicked from the party.");
        broadcastToParty(party, "§cA party member was left the party.");
    }

    public void toggleReady(@NotNull UUID playerId) {
        if (this.readyPlayers.contains(playerId)) {
            this.readyPlayers.remove(playerId);
        } else {
            this.readyPlayers.add(playerId);
        }

        Party party = this.getPartyOf(playerId);
        if (party == null) return;

        Player player = Bukkit.getPlayer(playerId);
        boolean isReady = this.readyPlayers.contains(playerId);
        broadcastToParty(party, "§e" + (player != null ? player.getName() : playerId) + " is " + (isReady ? "§aready" : "§cnot ready") + "§e.");
    }

    public boolean isPartyReady(@NotNull UUID leaderId) {
        Party party = this.getPartyByLeader(leaderId);
        if (party == null) return false;
        return party.isReady(this.readyPlayers);
    }

    public void setPendingQueue(@NotNull UUID leaderId, @NotNull String dungeonId) {
        this.pendingQueueRequest.put(leaderId, dungeonId);
    }

    @Nullable
    public String getPendingQueue(@NotNull UUID leaderId) {
        return this.pendingQueueRequest.get(leaderId);
    }

    public void clearPendingQueue(@NotNull UUID leaderId) {
        this.pendingQueueRequest.remove(leaderId);
    }

    public void resetReady(@NotNull UUID leaderId) {
        Party party = this.getPartyByLeader(leaderId);
        if (party == null) return;
        party.getAllMembers().forEach(this.readyPlayers::remove);
    }

    public void sendPartyInfo(@NotNull UUID playerId) {
        Party party = this.getPartyOf(playerId);
        if (party == null) return;

        removeOfflineMembers(party.getLeader());

        Player player = Bukkit.getPlayer(playerId);
        if (player == null) return;

        player.sendMessage("§6--- Party Info ---");
        Player leader = Bukkit.getPlayer(party.getLeader());
        player.sendMessage("§eLeader: §f" + (leader != null ? leader.getName() : party.getLeader()) + " " + (readyPlayers.contains(party.getLeader()) ? "§a[Ready]" : "§c[Not Ready]"));

        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            player.sendMessage("§eMember: §f" + (member != null ? member.getName() : memberId) + " " + (readyPlayers.contains(memberId) ? "§a[Ready]" : "§c[Not Ready]"));
        }

        String pendingDungeon = this.pendingQueueRequest.get(party.getLeader());
        if (pendingDungeon != null) {
            player.sendMessage("§ePending Queue: §f" + pendingDungeon);
        }

        player.sendMessage("§6------------------");
    }

    public void handlePlayerDisconnect(@NotNull UUID playerId) {
        if (!hasParty(playerId)) return;
        leaveParty(playerId);
    }

    public void broadcastToParty(@NotNull Party party, @NotNull String message) {
        for (UUID memberId : party.getAllMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null) member.sendMessage(message);
        }
    }



    public boolean hasParty(@NotNull UUID playerId) {
        return this.memberToLeader.containsKey(playerId);
    }

    public boolean hasPendingInvite(@NotNull UUID playerId) {
        return this.partyByLeader.values().stream().anyMatch(p -> p.hasPendingInvite(playerId));
    }

    @Nullable
    public Party getPartyByLeader(@NotNull UUID leaderId) {
        return this.partyByLeader.get(leaderId);
    }

    @Nullable
    public Party getPartyOf(@NotNull UUID playerId) {
        UUID leaderId = this.memberToLeader.get(playerId);
        if (leaderId == null) return null;
        return this.partyByLeader.get(leaderId);
    }

    public void addMember(@NotNull UUID leaderId, @NotNull UUID memberId) {
        Party party = this.partyByLeader.get(leaderId);
        if (party == null) return;

        party.addMember(memberId);
        this.memberToLeader.put(memberId, leaderId);
    }

    public void removeOfflineMembers(@NotNull UUID leaderId) {
        Party party = this.getPartyByLeader(leaderId);
        if (party == null) return;

        for (UUID memberId : new HashSet<>(party.getAllMembers())) {
            Player member = Bukkit.getPlayer(memberId);
            if (member == null || !member.isOnline()) {
                if (party.isLeader(memberId)) {
                    disbandParty(memberId);
                    return;
                } else {
                    kickMember(leaderId, memberId);
                }
            }
        }
    }

    public Collection<Party> getAllParties() {
        return this.partyByLeader.values();
    }


    @NotNull
    public Set<UUID> getReadyPlayers() {
        return readyPlayers;
    }
}