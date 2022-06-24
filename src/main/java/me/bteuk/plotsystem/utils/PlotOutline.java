package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.*;

public class PlotOutline {

    int index;
    BlockVector2 p2;
    double[] pos;
    int[] intPos;
    World world;
    Location l;

    int lengthX;
    int lengthZ;
    int length;

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
            pos[0] = p1.getX();
            pos[1] = p1.getZ();

            //Get length in x and z direction.
            lengthX = p2.getX()-p1.getX();
            lengthZ = p2.getZ()-p1.getZ();

            length = max(abs(lengthX), abs(lengthZ));

            //Iterate over the largest length of the two.
            for (int i = 0; i < length; i++) {

                //Set int position.
                intPos[0] = (int) (round(pos[0] + ((i*lengthX)/(double)length)));
                intPos[1] = (int) (round(pos[1] + ((i*lengthZ)/(double)length)));

                //Get location.
                l = new Location(world, intPos[0],
                        world.getHighestBlockYAt(intPos[0], intPos[1]), intPos[1]);

                //Add previous block to list.
                if (saveBlocks) {
                    previousBlocks.put(l, l.getBlock().getBlockData());
                }

                //Set fake block.
                p.sendBlockChange(l, blockType);

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
        lengthX = p2.getX()-p1.getX();
        lengthZ = p2.getZ()-p1.getZ();

        length = max(abs(lengthX), abs(lengthZ));

        //Iterate over the largest length of the two.
        for (int i = 0; i <= length; i++) {

            //Set int position.
            intPos[0] = (int) (round(pos[0] + ((i*lengthX)/(double)length)));
            intPos[1] = (int) (round(pos[1] + ((i*lengthZ)/(double)length)));

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
