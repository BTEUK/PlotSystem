package me.bteuk.plotsystem.utils.plugins;

import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.*;

import com.onarandombox.MultiverseCore.MultiverseCore;

public class Multiverse {

    public static boolean createVoidWorld(String name) {

        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (core == null) {
            Bukkit.getLogger().severe(Utils.chat("&cMultiverse is a dependency of PlotSystem!"));
            return false;
        }

        MVWorldManager worldManager = core.getMVWorldManager();

        worldManager.addWorld(
                name,
                World.Environment.NORMAL,
                null,
                WorldType.FLAT,
                false,
                "VoidGen:{biome:PLAINS}"
        );

        MultiverseWorld world = worldManager.getMVWorld(name);
        world.setGameMode(GameMode.CREATIVE);
        world.setAllowAnimalSpawn(false);
        world.setAllowMonsterSpawn(false);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setEnableWeather(false);
        world.setHunger(false);
        //TODO: disable daylightcycle

        //TODO worldguard protection

        Bukkit.getLogger().info("Created new world with name " + name);

        return true;
    }

    public static boolean hasWorld(String name) {

        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (core == null) {
            Bukkit.getLogger().severe(Utils.chat("&cMultiverse is a dependency of PlotSystem!"));
            return false;
        }

        //If the world exists return true.

        MVWorldManager worldManager = core.getMVWorldManager();

        MultiverseWorld world = worldManager.getMVWorld(name);

        if (world == null) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean deleteWorld(String name) {

        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (core == null) {
            Bukkit.getLogger().severe(Utils.chat("&cMultiverse is a dependency of PlotSystem!"));
            return false;
        }

        //If world exists delete it.
        MVWorldManager worldManager = core.getMVWorldManager();

        MultiverseWorld world = worldManager.getMVWorld(name);

        if (world == null) {
            return false;
        } else {
            worldManager.deleteWorld(name);
            return true;
        }

    }

    public static boolean unloadWorld(String name) {

        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (core == null) {
            Bukkit.getLogger().severe(Utils.chat("&cMultiverse is a dependency of PlotSystem!"));
            return false;
        }

        //If world exists delete it.
        MVWorldManager worldManager = core.getMVWorldManager();

        MultiverseWorld world = worldManager.getMVWorld(name);

        if (world == null) {
            return false;
        } else {
            worldManager.unloadWorld(name);
            return true;
        }

    }

    public static boolean loadWorld(String name) {

        MultiverseCore core = (MultiverseCore) Bukkit.getServer().getPluginManager().getPlugin("Multiverse-Core");

        if (core == null) {
            Bukkit.getLogger().severe(Utils.chat("&cMultiverse is a dependency of PlotSystem!"));
            return false;
        }

        //If world exists delete it.
        MVWorldManager worldManager = core.getMVWorldManager();

        MultiverseWorld world = worldManager.getMVWorld(name);

        if (world == null) {
            return false;
        } else {
            worldManager.loadWorld(name);
            return true;
        }

    }
}
