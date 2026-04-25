package su.nightexpress.dungeons.dungeon.classes;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ClassManager {

    private final Set<String> validClasses = new HashSet<>();
    private final NamespacedKey classKey;

    public ClassManager(JavaPlugin plugin, FileConfiguration config) {
        this.classKey = new NamespacedKey(plugin, "class");
        load(config);
    }

    public void load(FileConfiguration config) {
        validClasses.clear();

        if (!config.contains("class")) {
            throw new IllegalStateException("Missing 'class' in class.yml");
        }

        for (String s : config.getStringList("class")) {
            validClasses.add(s.toLowerCase());
        }
    }

    // -------------------------
    // CLASS API
    // -------------------------

    public boolean setClass(Player player, String className) {
        className = className.toLowerCase();

        if (!validClasses.contains(className)) {
            player.sendMessage("§cInvalid class!");
            return false;
        }

        player.getPersistentDataContainer().set(
                classKey,
                PersistentDataType.STRING,
                className
        );

        return true;
    }

    public String getClass(Player player) {
        String playerClass = player.getPersistentDataContainer().get(classKey, PersistentDataType.STRING);
        return playerClass != null ? playerClass : "warrior"; // Default class?
    }

    public void removeClass(Player player) {
        player.getPersistentDataContainer().remove(classKey);
    }

    public boolean hasClass(Player player) {
        return player.getPersistentDataContainer().has(classKey, PersistentDataType.STRING);
    }

    public Set<String> getValidClasses() {
        return Collections.unmodifiableSet(validClasses);
    }
}