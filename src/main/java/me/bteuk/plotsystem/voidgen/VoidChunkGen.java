package me.bteuk.plotsystem.voidgen;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.apache.commons.lang.StringUtils;
import org.bukkit.block.Biome;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class VoidChunkGen extends ChunkGen {

    public VoidChunkGen(JavaPlugin javaPlugin) {
        super(javaPlugin);

        this.chunkGenSettings = new ChunkGenSettings();
    }

    @Override
    public BiomeProvider getDefaultBiomeProvider(WorldInfo worldInfo) {
        if (Objects.isNull(this.chunkGenSettings.getBiome())) {
            return null;
        } else {
            return new VoidBiomeProvider();
        }
    }

    private static class VoidBiomeProvider extends BiomeProvider {
        private final Biome biome;

        public VoidBiomeProvider() {
            this.biome = Biome.PLAINS;
        }

        @Override
        public Biome getBiome(WorldInfo worldInfo, int x, int y, int z) {
            return this.biome;
        }

        @Override
        public List<Biome> getBiomes(WorldInfo worldInfo) {
            return Collections.singletonList(this.biome);
        }
    }
}
