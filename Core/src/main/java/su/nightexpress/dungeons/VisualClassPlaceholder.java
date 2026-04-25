package su.nightexpress.dungeons;

import org.bukkit.entity.Player;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;

public class VisualClassPlaceholder extends PlaceholderExpansion {

    @Override
    public String getIdentifier() {
        return "visualclass";
    }

    @Override
    public String getAuthor() {
        return "CacheCow";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        if (identifier.equalsIgnoreCase("name")) {
            return getVisualClass(player); // your logic here
        }

        return null;
    }

    private String getVisualClass(Player player) {
        return DungeonPlugin.instance.getClassManager().getClass(player);
    }
}
