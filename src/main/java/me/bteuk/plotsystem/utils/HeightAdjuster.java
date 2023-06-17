package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static me.bteuk.network.utils.Constants.MAX_Y;
import static me.bteuk.network.utils.Constants.MIN_Y;

public class HeightAdjuster {

    public static int[] getAdjustedYMinMax(List<BlockVector2> regionVector, @NotNull World world, int adjustedMin, int adjustedMax) {

        //Find the min and max point of the area.
        int min = MAX_Y;
        int max = MIN_Y;
        int elev;

        ProtectedPolygonalRegion region = new ProtectedPolygonalRegion("test", regionVector, 1, 1);

        //Iterate through every block and check the elevation.
        for (int i = region.getMinimumPoint().getBlockX(); i <= region.getMaximumPoint().getBlockX(); i++) {
            for (int j = region.getMinimumPoint().getBlockZ(); j <= region.getMaximumPoint().getBlockZ(); j++) {

                if (region.contains(BlockVector2.at(i, j))) {
                    elev = world.getHighestBlockYAt(i, j);

                    if (elev > max) {
                        max = elev;
                    }

                    if (elev < min) {
                        min = elev;
                    }
                }
            }
        }

        //Reduce max if it exceeds world limit.
        //Add adjustments from arguments.
        final int finalMax = Math.min(MAX_Y - 1, max + adjustedMax);
        final int finalMin = Math.max(MIN_Y, min + adjustedMin);


        return new int[]{finalMin, finalMax};

    }

    public static int[] getAdjustedYMinMax(int xMin, int xMax, int zMin, int zMax, @NotNull World world, int adjustedMin, int adjustedMax) {

        //Find the min and max point of the area.
        int min = MAX_Y;
        int max = MIN_Y;
        int elev;

        //Iterate through every block and check the elevation.
        for (int i = xMin; i <= xMax; i++) {
            for (int j = zMin; j <= zMax; j++) {

                elev = world.getHighestBlockYAt(i, j);

                if (elev > max) {
                    max = elev;
                }

                if (elev < min) {
                    min = elev;
                }
            }
        }

        //Reduce max if it exceeds world limit.
        final int finalMax = Math.min(MAX_Y - 1, max + adjustedMax);
        final int finalMin = Math.max(MIN_Y, min + adjustedMin);


        return new int[]{finalMin, finalMax};

    }
}
