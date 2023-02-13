package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

public class PlotOutline {

    double[] pos;
    int[] intPos;
    World world;
    Location l;

    int lengthX;
    int lengthZ;
    int length;

    int minX;
    int minZ;
    int maxX;
    int maxZ;

    HashMap<Location, BlockData> previousBlocks;

    public PlotOutline() {
        pos = new double[2];
        intPos = new int[2];
        previousBlocks = new HashMap<>();
    }

    public void createOutline(Player p, List<BlockVector2> corners, BlockData blockType, boolean saveBlocks) {

        //Set world
        world = p.getWorld();

        //Clear the hashmap.
        if (saveBlocks) {
            previousBlocks.clear();
        }

        //Create polygonal2dregion.
        Polygonal2DRegion region = new Polygonal2DRegion(BukkitAdapter.adapt(world), corners, 1, 1);

        minX = region.getMinimumPoint().getX();
        minZ = region.getMinimumPoint().getZ();

        maxX = region.getMaximumPoint().getX();
        maxZ = region.getMaximumPoint().getZ();

        //Iterate in the bounding box.
        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {

                //Check if the point is contained in the region.
                //If the previous point was not contained, set the block to be an outline.
                if (region.contains(i, j)) {

                    //Check if any of the surrounding blocks are not contained,
                    //if true then this block is an outline.
                    if (!(region.contains(i-1,j) && region.contains(i+1,j) && region.contains(i,j-1) && region.contains(i,j+1))) {

                        //Get location.
                        l = new Location(world, i,
                                world.getHighestBlockYAt(i, j), j);

                        //Add previous block to list.
                        if (saveBlocks) {
                            previousBlocks.put(l, l.getBlock().getBlockData());
                        }

                        //Set fake block.
                        p.sendBlockChange(l, blockType);

                    }
                }
            }
        }
    }

    public void revertBlocks(Player p) {

        //Set the blocks.
        for (Map.Entry<Location, BlockData> entry : previousBlocks.entrySet()) {

            p.sendBlockChange(entry.getKey(), entry.getValue());

        }
    }

    public void sendBlockChange(Player p, BlockVector2 bv, BlockData blockType) {

        //Set world
        world = p.getWorld();

        //Clear the hashmap.
        previousBlocks.clear();

        //Get location.
        l = new Location(world, bv.getX(),
                world.getHighestBlockYAt(bv.getX(), bv.getZ()), bv.getZ());

        //Add previous block to list.
        previousBlocks.put(l, l.getBlock().getBlockData());

        //Set fake block.
        p.sendBlockChange(l, blockType);

    }

    public void createLine(Player p, BlockVector2 p1, BlockVector2 p2, BlockData blockType) {

        //Set world
        world = p.getWorld();

        //Clean the hashmap
        previousBlocks.clear();

        //Get starting position.
        pos[0] = p1.getX();
        pos[1] = p1.getZ();

        //Get length in x and z direction.
        lengthX = p2.getX() - p1.getX();
        lengthZ = p2.getZ() - p1.getZ();

        length = max(abs(lengthX), abs(lengthZ));

        //Iterate over the largest length of the two.
        for (int i = 0; i <= length; i++) {

            //Set int position.
            intPos[0] = (int) (round(pos[0] + ((i * lengthX) / (double) length)));
            intPos[1] = (int) (round(pos[1] + ((i * lengthZ) / (double) length)));

            //Get location.
            l = new Location(world, intPos[0],
                    world.getHighestBlockYAt(intPos[0], intPos[1]), intPos[1]);

            //Add previous block to list.
            previousBlocks.put(l, l.getBlock().getBlockData());

            //Set fake block.
            p.sendBlockChange(l, blockType);

        }
    }
}
