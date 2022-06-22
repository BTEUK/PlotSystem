package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.List;

import static java.lang.Math.*;

public class PlotOutline {

    List<BlockVector2> corners;
    int index;
    BlockVector2 p2;
    double[] pos;
    int[] intPos;
    World world;
    Location l;

    int lengthX;
    int lengthZ;
    int length;

    public PlotOutline() {
        pos = new double[2];
        intPos = new int[2];
    }

    public void createPlotOutline(Player p, int plotID, BlockData blockType) {

        //Set world
        world = p.getWorld();

        //Get the corners of the plot.
        corners = WorldGuardFunctions.getPoints(plotID, world);

        //Iterate through the corners.
        for (BlockVector2 p1 : corners) {

            //Get the index of the corner in the list.
            index = corners.indexOf(p1);

            //If the index is the last corner then select the first as p2, else select the next corner as p2.
            if ((index + 1) == corners.size()) {
                p2 = corners.get(0);
            } else {
                p2 = corners.get((index + 1));
            }

            //Get starting position.
            pos[0] = min(p1.getX(),p2.getX());
            pos[1] = min(p1.getZ(),p2.getZ());

            //Get length in x and z direction.
            lengthX = abs(p1.getX() - p2.getX());
            lengthZ = abs(p1.getZ() - p2.getZ());

            length = max(lengthX, lengthZ);

            //Iterate over the largest length of the two.
            for (int i = 0; i < length; i++) {

                //Set int position.
                intPos[0] = (int) pos[0];
                intPos[1] = (int) pos[1];

                //Get location.
                l = new Location(world, intPos[0],
                        world.getHighestBlockYAt(intPos[0], intPos[1]), intPos[1]);

                //Set fake block.
                p.sendBlockChange(l, blockType);

            }
        }

    }


}
