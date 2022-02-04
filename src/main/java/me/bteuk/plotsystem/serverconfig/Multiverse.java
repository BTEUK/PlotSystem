package me.bteuk.plotsystem.serverconfig;

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
                null
        );

        MultiverseWorld world = worldManager.getMVWorld(name);
        world.setGameMode(GameMode.CREATIVE);
        world.setAllowAnimalSpawn(false);
        world.setAllowMonsterSpawn(false);
        world.setDifficulty(Difficulty.PEACEFUL);
        world.setEnableWeather(false);
        world.setHunger(false);

        Bukkit.getLogger().info("Created new world with name " + name);

        return true;
    }
}
