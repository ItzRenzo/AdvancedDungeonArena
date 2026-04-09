package su.nightexpress.dungeons.dungeon.Party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class PartyRecord {

    private final UUID partyLeader;
    private final Set<UUID> partyMembers;

    public PartyRecord(UUID playerUUID) {
        this.partyLeader = playerUUID;
        this.partyMembers = new HashSet<>();
    }

    public void addMember(UUID playerUniqueID) {
        partyMembers.add(playerUniqueID);
    }

    public void kickMember(UUID playerUniqueID) {
        partyMembers.remove(playerUniqueID);
    }

    public UUID getPartyLeader() {
        return partyLeader;
    }

    public Set<UUID> getPartyMembers() {
        return partyMembers;
    }
}