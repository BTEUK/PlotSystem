package me.bteuk.plotsystem.voidgen;

import com.google.gson.annotations.SerializedName;
import org.bukkit.block.Biome;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Random;

public abstract class ChunkGen extends ChunkGenerator {

    protected ChunkGenSettings chunkGenSettings;
    protected JavaPlugin javaPlugin;

    public ChunkGen(JavaPlugin paramPlugin) {
        this.javaPlugin = paramPlugin;
    }

    @Override
    public Location getFixedSpawnLocation(World world, Random random) {
        return new Location(world, 0d, 64d, 0d);
    }

    @Override
    public boolean shouldGenerateCaves() {
        return false;
    }

    @Override
    public boolean shouldGenerateDecorations() {
        return false;
    }

    @Override
    public boolean shouldGenerateMobs() {
        return false;
    }

    @Override
    public boolean shouldGenerateStructures() {
        return false;
    }

    @Override
    public boolean shouldGenerateNoise() {
        return false;
    }

    @Override
    public boolean shouldGenerateSurface() {
        return false;
    }

    @Override
    public boolean shouldGenerateBedrock() {
        return false;
    }

    @Override
    public void generateBedrock(WorldInfo worldInfo, Random random, int chunkX, int chunkZ, ChunkData chunkData) {
        // Bedrock block position
        final int x = 0, y = 64, z = 0;

        if ((x >= chunkX * 16) && (x < (chunkX + 1) * 16)) {
            if ((z >= chunkZ * 16) && (z < (chunkZ + 1) * 16)) {
                chunkData.setBlock(x, y, z, Material.BEDROCK);
            }
        }
    }
}
