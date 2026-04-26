package su.nightexpress.dungeons.kit;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import su.nightexpress.dungeons.DungeonPlugin;
import su.nightexpress.dungeons.dungeon.event.normal.DungeonJoinEvent;
import su.nightexpress.dungeons.dungeon.event.normal.DungeonLeftEvent;

import java.io.File;
import java.net.URL;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class OrbManager {

    private final JavaPlugin plugin;
    private FileConfiguration config;

    private final NamespacedKey ORB_KEY;
    private final NamespacedKey ORB_NAME_KEY;

    public OrbManager(JavaPlugin plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;
        this.ORB_KEY = new NamespacedKey(plugin, "orb");
        this.ORB_NAME_KEY = new NamespacedKey(plugin, "orb_name");
    }

    public void reload() {
        File orbFile = new File(plugin.getDataFolder(), "orb.yml");
        this.config = YamlConfiguration.loadConfiguration(orbFile);
    }

    public void giveOrbs(Player player, String visualClass) {

        ConfigurationSection section =
                config.getConfigurationSection("classes." + visualClass.toLowerCase());

        if (section == null) {
            plugin.getLogger().warning("[OrbManager] No section found for class: " + visualClass);
            section = config.getConfigurationSection("classes.default");
            if (section == null) return;
        }

        List<Map<?, ?>> orbs = section.getMapList("orbs");
        if (orbs.isEmpty()) return;

        PlayerInventory inv = player.getInventory();

        for (Map<?, ?> orb : orbs) {
            if (inv.firstEmpty() == -1) {
                plugin.getLogger().warning("[OrbManager] Inventory full for: " + player.getName());
                break;
            }

            ItemStack item = createOrb(player, orb, visualClass);

            Map<Integer, ItemStack> failed = inv.addItem(item);
            if (!failed.isEmpty()) {
                plugin.getLogger().warning("[OrbManager] Failed to add orb to inventory of: " + player.getName());
            } else {
                plugin.getLogger().info("[OrbManager] Successfully gave orb to: " + player.getName());
            }
        }

        player.updateInventory();
    }

    @EventHandler
    public void onDungeonJoin(DungeonJoinEvent event) {
        Player player = event.getPlayer();

        String visualClass = DungeonPlugin.instance.getClassManager().getClass(player);
        if (visualClass == null || visualClass.isEmpty()) return;

        DungeonPlugin.instance.getOrbManager().giveOrbs(player, visualClass);
    }

    @EventHandler
    public void onDungeonLeave(DungeonLeftEvent event) {
        DungeonPlugin.instance.getOrbManager().confiscateAll(event.getPlayer());
    }

    private ItemStack createOrb(Player player, Map<?, ?> orb, String className) {

        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) item.getItemMeta();
        if (meta == null) return item;

        String texture = (String) orb.get("texture");
        if (texture != null && !texture.isEmpty()) {
            applyTexture(meta, texture);
        }

        String orbName = (String) orb.get("orbname");
        meta.setDisplayName(color(orbName));

        if (orbName != null) {
            meta.getPersistentDataContainer().set(
                    ORB_NAME_KEY,
                    PersistentDataType.STRING,
                    orbName
            );
        }

        List<?> rawLore = (List<?>) orb.get("lore");
        if (rawLore != null) {
            meta.setLore(rawLore.stream()
                    .map(line -> color(line.toString()
                            .replace("%player%", player.getName())
                            .replace("%visualclass_name%", className)
                    ))
                    .toList());
        }

        meta.getPersistentDataContainer().set(
                ORB_KEY,
                PersistentDataType.BYTE,
                (byte) 1
        );

        item.setItemMeta(meta);
        return item;
    }

    private void applyTexture(SkullMeta meta, String texture) {
        try {
            String url;

            // Check if it's base64 or a raw hash
            if (texture.startsWith("http")) {
                url = texture;
            } else {
                try {
                    String decoded = new String(Base64.getDecoder().decode(texture));
                    url = decoded.split("\"url\":\"")[1].split("\"")[0];
                } catch (Exception e) {
                    // Assume it's a raw hash
                    url = "https://textures.minecraft.net/texture/" + texture;
                }
            }

            PlayerProfile profile = plugin.getServer().createPlayerProfile(UUID.randomUUID());
            PlayerTextures textures = profile.getTextures();
            textures.setSkin(new URL(url));
            profile.setTextures(textures);
            meta.setOwnerProfile(profile);

        } catch (Exception e) {
            plugin.getLogger().warning("Failed to apply orb texture: " + e.getMessage());
        }
    }

    public List<String> getOrbCommands(ItemStack item, String visualClass) {
        if (!isOrb(item)) return List.of();

        String orbName = item.getItemMeta()
                .getPersistentDataContainer()
                .get(ORB_NAME_KEY, PersistentDataType.STRING);

        if (orbName == null) return List.of();

        return getOrbList(visualClass).stream()
                .filter(o -> orbName.equals(o.get("orbname")))
                .findFirst()
                .map(o -> (List<String>) o.get("commands"))
                .orElse(List.of());
    }

    private List<Map<?, ?>> getOrbList(String visualClass) {
        ConfigurationSection section =
                config.getConfigurationSection("classes." + visualClass.toLowerCase());
        if (section == null) section = config.getConfigurationSection("classes.default");
        if (section == null) return List.of();
        return section.getMapList("orbs");
    }

    public void confiscateAll(Player player) {

        ItemStack[] contents = player.getInventory().getContents();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (isOrb(item)) {
                player.getInventory().setItem(i, null);
            }
        }

        player.updateInventory();
    }

    public boolean isOrb(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        return item.getItemMeta()
                .getPersistentDataContainer()
                .has(ORB_KEY, PersistentDataType.BYTE);
    }

    private String color(String text) {
        return text == null ? "" : text.replace("&", "§");
    }
}