package me.bteuk.plotsystem.utils.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.exceptions.WorldNotFoundException;
import me.bteuk.plotsystem.utils.math.Point;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.storage.StorageException;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.bteuk.plotsystem.sql.PlotSQL;

public class WorldGuardFunctions {

    public static Location getCurrentLocation(String regionName, World world) throws RegionNotFoundException, RegionManagerNotFoundException {

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + world.getName() + " is null!");

        }

        //Get the worldguard region and teleport to player to one of the corners.
        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);

        if (region == null) {

            throw new RegionNotFoundException("Region " + regionName + " does not exist!");

        }

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        return (new Location(world, bv.getX(), Utils.getHighestYAt(world, bv.getX(), bv.getZ()), bv.getZ()));

    }

    public static Location getBeforeLocation(String regionName, World buildWorld) throws WorldNotFoundException, RegionNotFoundException, RegionManagerNotFoundException {

        //Get instance of plugin and config
        PlotSystem instance = PlotSystem.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds from config
        String save_world = config.getString("save_world");
        if (save_world == null) {

            throw new WorldNotFoundException("Save World is not defined in config, plot delete event has therefore failed!");

        }

        World saveWorld = Bukkit.getServer().getWorld(save_world);

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(buildWorld));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + buildWorld.getName() + " is null!");

        }

        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);

        if (region == null) {

            throw new RegionNotFoundException("Region " + regionName + " does not exist!");

        }

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        //To get the actual location we need to take the negative coordinate transform of the plot.
        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

        int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + buildWorld.getName() + "';");
        int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + buildWorld.getName() + "';");

        BlockVector2 bv2 = BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform);

        return (new Location(saveWorld, bv2.getX(), Utils.getHighestYAt(saveWorld, bv2.getX(), bv2.getZ()), bv2.getZ()));

    }

    public static List<BlockVector2> getPoints(String regionName, World world) throws RegionNotFoundException, RegionManagerNotFoundException {

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + world.getName() + " is null!");

        }

        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);

        if (region == null) {

            throw new RegionNotFoundException("Region " + regionName + " does not exist!");

        }

        return region.getPoints();

    }

    public static boolean inRegion(Block block) throws RegionManagerNotFoundException {

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionManager regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(block.getWorld()));

        if (regions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + block.getWorld().getName() + " is null!");

        }

        //Get the blockvector3 at the block.
        BlockVector3 v = BlockVector3.at(block.getX(), block.getY(), block.getZ());

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = regions.getApplicableRegions(v);

        return set.size() > 0;
    }

    public static boolean addMember(String regionName, String uuid, World world) throws RegionManagerNotFoundException, RegionNotFoundException {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + world.getName() + " is null!");

        }

        ProtectedRegion region = buildRegions.getRegion(regionName);

        if (region == null) {

            throw new RegionNotFoundException("Region " + regionName + " does not exist!");

        }

        //Add the member to the region.
        region.getMembers().addPlayer(UUID.fromString(uuid));

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public static void removeMember(String regionName, String uuid, World world) throws RegionManagerNotFoundException, RegionNotFoundException {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + world.getName() + " is null!");

        }

        //Check if the member is in the region.
        ProtectedRegion region = buildRegions.getRegion(regionName);

        if (region == null) {

            throw new RegionNotFoundException("Region " + regionName + " does not exist!");

        }

        if (region.getMembers().contains(UUID.fromString(uuid))) {
            //Remove the member to the region.
            region.getMembers().removePlayer(UUID.fromString(uuid));
        } else {
            return;
        }

        //Save the changes
        try {
            buildRegions.saveChanges();
        } catch (StorageException e1) {
            e1.printStackTrace();
        }
    }

    public static void clearMembers(String regionName, World world) throws RegionNotFoundException, RegionManagerNotFoundException {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + world.getName() + " is null!");

        }

        ProtectedRegion region = buildRegions.getRegion(regionName);

        if (region == null) {

            throw new RegionNotFoundException("Region " + regionName + " does not exist!");

        }

        //Remove all members from the region.
        region.getMembers().clear();

        //Save the changes
        try {
            buildRegions.saveChanges();
        } catch (StorageException e1) {
            e1.printStackTrace();
        }
    }

    public static boolean delete(String regionName, World world) throws RegionManagerNotFoundException {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            throw new RegionManagerNotFoundException("RegionManager for world " + world.getName() + " is null!");

        }

        //Get the region to remove the outlines.
        ProtectedRegion region = buildRegions.getRegion(regionName);

        if (region != null) {
            PlotSystem.getInstance().getOutlines().removeOutline(region, world);
        }

        //Attempt to remove the plot.
        buildRegions.removeRegion(regionName);

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    /**
     * Get the points of a specific plot or zone as if it was located in the save world.
     * This is done by getting the points in the world where the plot or zone is and then applying the negative transform from its original location.
     *
     * @param regionName
     * the name of the plot or zone
     * @param world
     * the name of the world where the plot or zone exists, NOT the world of the save world
     */
    public static List<BlockVector2> getPointsTransformedToSaveWorld(String regionName, World world) throws RegionNotFoundException, RegionManagerNotFoundException {

        List<BlockVector2> vector = getPoints(regionName, world);
        List<BlockVector2> newVector = new ArrayList<>();

        //Get the negative coordinate transform.
        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

        int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + world.getName() + "';");
        int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + world.getName() + "';");

        //Apply to transform to each coordinate.
        vector.forEach(bv -> newVector.add(BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform)));

        return newVector;

    }
}
