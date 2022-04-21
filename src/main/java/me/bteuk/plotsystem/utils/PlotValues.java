package me.bteuk.plotsystem.utils;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.enums.PlotDifficulty;
import me.bteuk.plotsystem.utils.enums.PlotSize;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

public class PlotValues {

    //Returns the plot difficulty name.
    public static PlotDifficulty difficultyName(int difficulty) {

        switch (difficulty) {

            case 1:
                return PlotDifficulty.EASY;
            case 2:
                return PlotDifficulty.NORMAL;
            case 3:
                return PlotDifficulty.HARD;
            default:
                return null;

        }
    }

    //Returns the plot size name.
    public static PlotSize sizeName(int size) {

        switch (size) {

            case 1:
                return PlotSize.SMALL;
            case 2:
                return PlotSize.MEDIUM;
            case 3:
                return PlotSize.LARGE;
            default:
                return null;

        }
    }

    //Returns the plot size material.
    public static Material sizeMaterial(int size) {

        switch (size) {

            case 1:
                return Material.LIME_CONCRETE;
            case 2:
                return Material.YELLOW_CONCRETE;
            case 3:
                return Material.RED_CONCRETE;
            default:
                return null;

        }
    }

    //Returns the plot difficulty material.
    public static Material difficultyMaterial(int difficulty) {

        switch (difficulty) {

            case 1:
                return Material.LIME_CONCRETE;
            case 2:
                return Material.YELLOW_CONCRETE;
            case 3:
                return Material.RED_CONCRETE;
            default:
                return null;

        }
    }

    public static int sizeValue(int size) {

        FileConfiguration config = PlotSystem.getInstance().getConfig();

        switch (size) {

            case 1:

                return config.getInt("size.small");

            case 2:

                return config.getInt("size.medium");

            case 3:

                return config.getInt("size.large");

        }

        Bukkit.getLogger().warning(Utils.chat("&cPlot size was not in the range of possible values!"));
        return 0;

    }

    public static int difficultyValue(int difficulty) {

        FileConfiguration config = PlotSystem.getInstance().getConfig();

        switch (difficulty) {

            case 1:

                return config.getInt("difficulty.easy");

            case 2:

                return config.getInt("difficulty.normal");

            case 3:

                return config.getInt("difficulty.hard");

        }

        Bukkit.getLogger().warning(Utils.chat("&cPlot difficulty was not in the range of possible values!"));
        return 0;

    }
}
