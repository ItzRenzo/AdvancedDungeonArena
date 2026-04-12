package su.nightexpress.dungeons.dungeon.Party;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Party {

    private final UUID leader;
    private final List<UUID> members;
    private final Map<UUID, Long> pendingInvites;

    private static final long INVITE_TIMEOUT = 30_000L;

    public Party(@NotNull UUID leader) {
        this.leader = leader;
        this.members = new ArrayList<>();
        this.pendingInvites = new HashMap<>();
    }

    public boolean isLeader(@NotNull UUID playerId) {
        return this.leader.equals(playerId);
    }

    public boolean isMember(@NotNull UUID playerId) {
        return this.members.contains(playerId) || this.leader.equals(playerId);
    }

    public boolean hasPendingInvite(@NotNull UUID playerId) {
        cleanExpiredInvites();
        return this.pendingInvites.containsKey(playerId);
    }

    public void addInvite(@NotNull UUID playerId) {
        this.pendingInvites.put(playerId, System.currentTimeMillis() + INVITE_TIMEOUT);
    }

    public void removeInvite(@NotNull UUID playerId) {
        this.pendingInvites.remove(playerId);
    }

    public void addMember(@NotNull UUID playerId) {
        if (!this.members.contains(playerId)) {
            this.members.add(playerId);
        }
    }

    public void removeMember(@NotNull UUID playerId) {
        this.members.remove(playerId);
    }

    public boolean isReady(@NotNull Set<UUID> readyPlayers) {
        if (!readyPlayers.contains(this.leader)) return false;
        return readyPlayers.containsAll(this.members);
    }

    private void cleanExpiredInvites() {
        long now = System.currentTimeMillis();
        this.pendingInvites.entrySet().removeIf(e -> e.getValue() < now);
    }

    @NotNull
    public UUID getLeader() {
        return leader;
    }

    @NotNull
    public List<UUID> getMembers() {
        return members;
    }

    @NotNull
    public List<UUID> getAllMembers() {
        List<UUID> all = new ArrayList<>();
        all.add(leader);
        all.addAll(members);
        return all;
    }

    public int getSize() {
        return 1 + members.size();
    }
}