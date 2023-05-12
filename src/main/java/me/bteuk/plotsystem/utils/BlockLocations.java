package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.regions.Polygonal2DRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.ArrayList;

//A list of block locations, can be altered in bulk.
public class BlockLocations {

    //Store the world of the player.
    private final Player player;
    private World world;
    private ArrayList<BlockLocation> locations;

    public BlockLocations(Player player) {

        this.player = player;
        this.world = player.getWorld();
        locations = new ArrayList<>();

    }

    //Add all the block locations that make up the outline of the region.
    public void addOutline(ProtectedRegion region, BlockData block) {

        int minX = region.getMinimumPoint().getX();
        int minZ = region.getMinimumPoint().getZ();

        int maxX = region.getMaximumPoint().getX();
        int maxZ = region.getMaximumPoint().getZ();

        //Iterate in the bounding box.
        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {

                //Check if the point is contained in the region.
                //If the previous point was not contained, set the block to be an outline.
                if (region.contains(BlockVector2.at(i, j))) {

                    //Check if any of the surrounding blocks are not contained,
                    //if true then this block is an outline.
                    if (!(region.contains(BlockVector2.at(i - 1, j)) && region.contains(BlockVector2.at(i + 1, j)) && region.contains(BlockVector2.at(i, j - 1)) && region.contains(BlockVector2.at(i, j + 1)))) {

                        locations.add(new BlockLocation(i, j, block));

                    }
                }
            }
        }
    }

    //Remove all the block locations that make up the outline of the region.
    //Additionally replace the fake block with air.
    public void removeOutline(ProtectedRegion region, BlockData block) {

        int minX = region.getMinimumPoint().getX();
        int minZ = region.getMinimumPoint().getZ();

        int maxX = region.getMaximumPoint().getX();
        int maxZ = region.getMaximumPoint().getZ();

        //Iterate in the bounding box.
        for (int i = minX; i <= maxX; i++) {
            for (int j = minZ; j <= maxZ; j++) {

                //Check if the point is contained in the region.
                //If the previous point was not contained, set the block to be an outline.
                if (region.contains(BlockVector2.at(i, j))) {

                    //Check if any of the surrounding blocks are not contained,
                    //if true then this block is an outline.
                    if (!(region.contains(BlockVector2.at(i - 1, j)) && region.contains(BlockVector2.at(i + 1, j)) && region.contains(BlockVector2.at(i, j - 1)) && region.contains(BlockVector2.at(i, j + 1)))) {

                        player.sendBlockChange(
                                new Location(
                                        world, i, (world.getHighestBlockYAt(i, j) + 1), j
                                ), Material.AIR.createBlockData()
                        );

                        locations.remove(new BlockLocation(i, j, block));

                    }
                }
            }
        }

    }

    //Remove all block locations from the list.
    //This would be used when the player moves a certain number of blocks.
    public void clear() {
        locations.clear();
    }

    //Add all the block locations that make up the line.

    //Remove all the block locations that make up the line.

    //Draw all blocks for a specific player.
    public void drawOutlines() {

        for (BlockLocation bl : locations) {

            player.sendBlockChange(
                    new Location(
                            world, bl.getX(),
                            (world.getHighestBlockYAt(bl.getX(), bl.getZ())),
                            bl.getZ()
                    ), bl.getBlock()
            );

        }
    }

}
