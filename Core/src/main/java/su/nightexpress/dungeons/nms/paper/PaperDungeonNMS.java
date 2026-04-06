package su.nightexpress.dungeons.nms.paper;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import su.nightexpress.dungeons.api.dungeon.Dungeon;
import su.nightexpress.dungeons.api.schema.SchemaBlock;
import su.nightexpress.dungeons.api.type.MobFaction;
import su.nightexpress.dungeons.nms.DungeonNMS;
import su.nightexpress.nightcore.util.geodata.pos.BlockPos;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class PaperDungeonNMS implements DungeonNMS {

    private static final int SCHEMA_MAGIC = 0x41444132; // ADA2

    @Override
    public boolean isSupportedMob(@NotNull EntityType type) {
        Class<? extends Entity> entityClass = type.getEntityClass();
        return type.isAlive() && type.isSpawnable() && entityClass != null && LivingEntity.class.isAssignableFrom(entityClass);
    }

    @Override
    @Nullable
    public LivingEntity spawnMob(@NotNull Dungeon dungeon,
                                 @NotNull EntityType type,
                                 @NotNull MobFaction faction,
                                 @NotNull Location location,
                                 @NotNull Consumer<LivingEntity> function) {
        World world = location.getWorld();
        if (world == null) return null;

        Entity entity;
        try {
            entity = world.spawnEntity(location, type);
        }
        catch (IllegalArgumentException ignored) {
            return null;
        }

        if (!(entity instanceof LivingEntity livingEntity)) {
            entity.remove();
            return null;
        }

        function.accept(livingEntity);
        return livingEntity;
    }

    @Override
    @Nullable
    public EntityType getSpawnEggType(@NotNull ItemStack itemStack) {
        Material material = itemStack.getType();
        String materialName = material.name();
        if (!materialName.endsWith("_SPAWN_EGG")) {
            return null;
        }

        String entityKey = materialName.substring(0, materialName.length() - "_SPAWN_EGG".length()).toLowerCase();
        return Bukkit.getRegistry(EntityType.class).get(NamespacedKey.minecraft(entityKey));
    }

    @Override
    public void setSchemaBlock(@NotNull World world, @NotNull SchemaBlock schemaBlock) {
        BlockPos pos = schemaBlock.getBlockPos();
        Block block = world.getBlockAt(pos.getX(), pos.getY(), pos.getZ());
        block.setBlockData(schemaBlock.getBlockData(), false);
    }

    @NotNull
    @Override
    public List<SchemaBlock> loadSchema(@NotNull File file, boolean compressed) {
        List<SchemaBlock> schemaBlocks = new ArrayList<>();
        if (!file.exists()) {
            return schemaBlocks;
        }

        try (DataInputStream input = this.openInput(file, compressed)) {
            int magic = input.readInt();
            if (magic != SCHEMA_MAGIC) {
                return schemaBlocks;
            }

            int size = input.readInt();
            for (int i = 0; i < size; i++) {
                int x = input.readInt();
                int y = input.readInt();
                int z = input.readInt();
                String dataString = input.readUTF();

                // Reserved for tile-entity payload in future schema versions.
                input.readBoolean();

                BlockData blockData = Bukkit.createBlockData(dataString);
                schemaBlocks.add(new SchemaBlock(new BlockPos(x, y, z), blockData, null));
            }
        }
        catch (EOFException ignored) {
            return new ArrayList<>();
        }
        catch (IllegalArgumentException | IOException exception) {
            exception.printStackTrace();
        }

        return schemaBlocks;
    }

    @Override
    public void saveSchema(@NotNull World world, @NotNull List<Block> blocks, @NotNull File file) {
        try (DataOutputStream output = this.openOutput(file)) {
            output.writeInt(SCHEMA_MAGIC);
            output.writeInt(blocks.size());

            for (Block block : blocks) {
                output.writeInt(block.getX());
                output.writeInt(block.getY());
                output.writeInt(block.getZ());
                output.writeUTF(block.getBlockData().getAsString());
                output.writeBoolean(false);
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @NotNull
    private DataInputStream openInput(@NotNull File file, boolean compressed) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        BufferedInputStream buffered = new BufferedInputStream(stream);
        return compressed ? new DataInputStream(new GZIPInputStream(buffered)) : new DataInputStream(buffered);
    }

    @NotNull
    private DataOutputStream openOutput(@NotNull File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        BufferedOutputStream buffered = new BufferedOutputStream(stream);
        return new DataOutputStream(new GZIPOutputStream(buffered));
    }
}
