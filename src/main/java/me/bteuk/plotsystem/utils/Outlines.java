package me.bteuk.plotsystem.utils;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;

//This class deals with plot and zone outlines.
//It will have method to refresh outlines.
public class Outlines {

    //List of block locations where outlines should be generated when the outlines are refreshed.
    //The y-level is calculated when the block is placed, as this could change often.
    HashMap<Player, BlockLocations> outlineBlockLocations ;

    WorldGuard wg;

    public Outlines() {

        outlineBlockLocations = new HashMap<>();

        wg = WorldGuard.getInstance();

    }

    //Add player
    public void addPlayer(Player player) {
        if (!outlineBlockLocations.containsKey(player)) {
            outlineBlockLocations.put(player, new BlockLocations(player));
        }
    }

    //Remove player
    public void removePlayer(Player player) {
        outlineBlockLocations.remove(player);
    }


    //Reloads the outlines for a specific player.
    public void refreshOutlinesForPlayer(Player player) throws NullPointerException {

        outlineBlockLocations.get(player).drawOutlines();

    }

    //Get all outlines near the player, remove all existing outlines from the object, but don't bother removing the blocks.
    //This method assumed the actual regions have not changed,
    // only that the player has moved position sufficiently that new outlines need to be drawn.
    public void addNearbyOutlines(Player player) {

        //If the player does not have a key, add it.
        BlockLocations locations;
        if (outlineBlockLocations.containsKey(player)) {
            locations = outlineBlockLocations.get(player);
            locations.clear();
        } else {
            locations = new BlockLocations(player);
            outlineBlockLocations.put(player, locations);
        }

        //Find the nearby regions and add them to the locations.
        RegionManager regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(player.getWorld()));

        if (regions == null) {return;}

        //Get all regions within 100 blocks of the player.
        ProtectedRegion region = new ProtectedCuboidRegion("test",
                BlockVector3.at(player.getLocation().getX() - 100, 1, player.getLocation().getZ() - 100),
                BlockVector3.at(player.getLocation().getX() + 100, 1, player.getLocation().getZ() + 100));
        ApplicableRegionSet set = regions.getApplicableRegions(region);

        //Iterate through the regions and add the outlines.
        for (ProtectedRegion protectedRegion : set) {

            int plotID = tryParse(protectedRegion.getId());

            //If plotID is 0, then it's a zone.
            if (plotID == 0) {

                locations.addOutline(region, Material.PURPLE_CONCRETE.createBlockData());

            } else {

                //Get plot difficulty.
                int difficulty = PlotSystem.getInstance().plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";");

                locations.addOutline(region, difficultyMaterial(difficulty));

            }
        }
    }

    //Returns the plot difficulty material.
    public BlockData difficultyMaterial(int difficulty) {

        return switch (difficulty) {
            case 1 -> Material.LIME_CONCRETE.createBlockData();
            case 2 -> Material.YELLOW_CONCRETE.createBlockData();
            case 3 -> Material.RED_CONCRETE.createBlockData();
            default -> null;
        };
    }

    public int tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
