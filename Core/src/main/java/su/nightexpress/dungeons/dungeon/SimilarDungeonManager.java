package su.nightexpress.dungeons.dungeon;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;


public class SimilarDungeonManager {

    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;

    /** In-memory map: groupId -> list of dungeon names in that group */
    private final Map<Integer, List<String>> groups = new HashMap<>();

    private static final String SECTION = "similar-dungeons";

    public SimilarDungeonManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "similar_dungeons.yml");
        load();
    }

    // -------------------------------------------------------------------------
    // Load / Save
    // -------------------------------------------------------------------------

    /** Loads (or creates) similar_dungeons.yml and populates the in-memory map. */
    public void load() {
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create similar_dungeons.yml: " + e.getMessage());
            }
        }

        config = YamlConfiguration.loadConfiguration(configFile);
        groups.clear();

        if (config.isConfigurationSection(SECTION)) {
            for (String key : config.getConfigurationSection(SECTION).getKeys(false)) {
                try {
                    int id = Integer.parseInt(key);
                    List<String> dungeons = config.getStringList(SECTION + "." + key);
                    groups.put(id, new ArrayList<>(dungeons));
                } catch (NumberFormatException e) {
                    plugin.getLogger().warning("Non-integer group key '" + key + "' in similar_dungeons.yml – skipped.");
                }
            }
        }
    }

    /** Saves the in-memory map back to similar_dungeons.yml. */
    public void save() {
        // Wipe the section so removed groups don't linger
        config.set(SECTION, null);

        for (Map.Entry<Integer, List<String>> entry : groups.entrySet()) {
            config.set(SECTION + "." + entry.getKey(), entry.getValue());
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save similar_dungeons.yml: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    /**
     * Returns all dungeons in the same group as {@code dungeonName},
     * excluding {@code dungeonName} itself.
     *
     * @param dungeonName the dungeon to look up
     * @return list of similar dungeon names, or an empty list if none found
     */
    public List<String> getSimilar(String dungeonName) {
        for (List<String> group : groups.values()) {
            if (containsIgnoreCase(group, dungeonName)) {
                List<String> result = new ArrayList<>(group);
                result.removeIf(d -> d.equalsIgnoreCase(dungeonName));
                return result;
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the full group (including the queried dungeon) that contains
     * {@code dungeonName}, or an empty list if not found.
     */
    public List<String> getFullGroup(String dungeonName) {
        for (List<String> group : groups.values()) {
            if (containsIgnoreCase(group, dungeonName)) {
                return new ArrayList<>(group);
            }
        }
        return Collections.emptyList();
    }

    /**
     * Returns the group ID that contains {@code dungeonName}, or -1 if not found.
     */
    public int getGroupId(String dungeonName) {
        for (Map.Entry<Integer, List<String>> entry : groups.entrySet()) {
            if (containsIgnoreCase(entry.getValue(), dungeonName)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    /** Returns true if {@code dungeonName} belongs to any group. */
    public boolean isGrouped(String dungeonName) {
        return getGroupId(dungeonName) != -1;
    }

    /** Returns an unmodifiable view of all groups. */
    public Map<Integer, List<String>> getAllGroups() {
        return Collections.unmodifiableMap(groups);
    }

    // -------------------------------------------------------------------------
    // Mutate
    // -------------------------------------------------------------------------

    /**
     * Adds a new group with the provided dungeon names.
     * The group ID is auto-incremented (next available integer).
     *
     * @return the new group ID
     */
    public int addGroup(String... dungeonNames) {
        return addGroup(Arrays.asList(dungeonNames));
    }

    /**
     * Adds a new group with the provided dungeon names.
     *
     * @return the new group ID
     */
    public int addGroup(List<String> dungeonNames) {
        int newId = groups.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        groups.put(newId, new ArrayList<>(dungeonNames));
        save();
        return newId;
    }

    /**
     * Adds {@code dungeonName} to an existing group. Does nothing if the
     * dungeon is already in that group.
     */
    public void addToGroup(int groupId, String dungeonName) {
        List<String> group = groups.computeIfAbsent(groupId, k -> new ArrayList<>());
        if (!containsIgnoreCase(group, dungeonName)) {
            group.add(dungeonName);
            save();
        }
    }

    /**
     * Removes {@code dungeonName} from whichever group it belongs to.
     * If the group becomes empty it is deleted.
     *
     * @return true if the dungeon was found and removed
     */
    public boolean removeDungeon(String dungeonName) {
        Iterator<Map.Entry<Integer, List<String>>> it = groups.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, List<String>> entry = it.next();
            List<String> group = entry.getValue();
            if (group.removeIf(d -> d.equalsIgnoreCase(dungeonName))) {
                if (group.isEmpty()) it.remove();
                save();
                return true;
            }
        }
        return false;
    }

    /** Removes the entire group by ID. */
    public boolean removeGroup(int groupId) {
        if (groups.remove(groupId) != null) {
            save();
            return true;
        }
        return false;
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private boolean containsIgnoreCase(List<String> list, String value) {
        for (String s : list) {
            if (s.equalsIgnoreCase(value)) return true;
        }
        return false;
    }
}
