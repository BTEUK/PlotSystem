package me.bteuk.plotsystem.utils.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.math.Point;
import me.bteuk.plotsystem.utils.User;
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
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;

import me.bteuk.plotsystem.sql.PlotSQL;

public class WorldGuardFunctions {

    public static Location getCurrentLocation(String regionName, World world) {

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            PlotSystem.getInstance().getLogger().warning("RegionManager for world " + world.getName() + " is null!");
            return null;

        }

        //Get the worldguard region and teleport to player to one of the corners.
        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);

        if (region == null) {

            PlotSystem.getInstance().getLogger().warning("Region " + regionName + " does not exist!");
            return null;

        }

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        return (new Location(world, bv.getX(), Utils.getHighestYAt(world, bv.getX(), bv.getZ()), bv.getZ()));

    }

    public static Location getBeforeLocation(String regionName, World buildWorld) {

        //Get instance of plugin and config
        PlotSystem instance = PlotSystem.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds from config
        String save_world = config.getString("save_world");
        if (save_world == null) {
            PlotSystem.getInstance().getLogger().warning("Save World is not defined in config, plot delete event has therefore failed!");
            return null;
        }

        World saveWorld = Bukkit.getServer().getWorld(save_world);

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(buildWorld));

        if (buildRegions == null) {

            PlotSystem.getInstance().getLogger().warning("RegionManager for world " + buildWorld.getName() + " is null!");
            return null;

        }

        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);

        if (region == null) {

            PlotSystem.getInstance().getLogger().warning("Region " + regionName + " does not exist!");
            return null;

        }

        BlockVector2 bv = Point.getAveragePoint(region.getPoints());

        //To get the actual location we need to take the negative coordinate transform of the plot.
        PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

        int xTransform = -plotSQL.getInt("SELECT xTransform FROM location_data WHERE name='" + buildWorld.getName() + "';");
        int zTransform = -plotSQL.getInt("SELECT zTransform FROM location_data WHERE name='" + buildWorld.getName() + "';");

        BlockVector2 bv2 = BlockVector2.at(bv.getX() + xTransform, bv.getZ() + zTransform);

        return (new Location(saveWorld, bv2.getX(), Utils.getHighestYAt(saveWorld, bv2.getX(), bv2.getZ()), bv2.getZ()));

    }

    public static List<BlockVector2> getPoints(String regionName, World world) {

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            PlotSystem.getInstance().getLogger().warning("RegionManager for world " + world.getName() + " is null!");
            return null;

        }

        ProtectedPolygonalRegion region = (ProtectedPolygonalRegion) buildRegions.getRegion(regionName);

        if (region == null) {

            PlotSystem.getInstance().getLogger().warning("Region " + regionName + " does not exist!");
            return null;

        }

        return region.getPoints();

    }

    public static boolean inRegion(Block block) {

        //Get worldguard instance
        WorldGuard wg = WorldGuard.getInstance();

        //Get worldguard region data
        RegionManager regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(block.getWorld()));

        //Get the blockvector3 at the block.
        BlockVector3 v = BlockVector3.at(block.getX(), block.getY(), block.getZ());

        //Check whether the region overlaps an existing plot, if true stop the process.
        ApplicableRegionSet set = regions.getApplicableRegions(v);

        return set.size() > 0;
    }

    public static ApplicableRegionSet getPlots(BlockVector3 min, BlockVector3 max, int radius) {

        //Get plugin instance and config.
        PlotSystem instance = PlotSystem.getInstance();
        FileConfiguration config = instance.getConfig();

        //Get worlds.
        World saveWorld = Bukkit.getWorld(config.getString("worlds.save"));

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager saveRegions = container.get(BukkitAdapter.adapt(saveWorld));

        //Create region
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("check", BlockVector3.at(min.getX() - radius, 1, min.getZ() - radius), BlockVector3.at(max.getX() + radius, 256, max.getZ() + radius));

        //Check whether the region overlaps an existing plot, if true stop the process.
        return saveRegions.getApplicableRegions(region);
    }

    public static ArrayList<Integer> getNearbyPlots(User u) {

        //Create HashMap
        ArrayList<Integer> list = new ArrayList<>();

        BlockVector3 pos = BlockVector3.at(u.player.getLocation().getX(), u.player.getLocation().getY(), u.player.getLocation().getZ());

        ApplicableRegionSet set = getPlots(pos, pos, 100);

        if (set.size() == 0) {
            return list;
        }

        for (ProtectedRegion entry : set) {
            if (!(entry.getOwners().contains(UUID.fromString(u.uuid)))) {
                list.add(Integer.parseInt(entry.getId()));
            }
        }

        return list;
    }

    public static ArrayList<Integer> getNearbyPlots(ProtectedPolygonalRegion check) {

        //Create HashMap
        ArrayList<Integer> list = new ArrayList<>();

        ApplicableRegionSet set = getPlots(check.getMinimumPoint(), check.getMaximumPoint(), 5);

        if (set.size() == 0) {
            return list;
        }

        for (ProtectedRegion entry : set) {
            list.add(Integer.parseInt(entry.getId()));
        }

        return list;
    }

    public static boolean addMember(int plot, String uuid, World world) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        //Add the member to the region.
        buildRegions.getRegion(String.valueOf(plot)).getMembers().addPlayer(UUID.fromString(uuid));

        //Save the changes
        try {
            buildRegions.saveChanges();
            return true;
        } catch (StorageException e1) {
            e1.printStackTrace();
            return false;
        }
    }

    public static void removeMember(String regionName, String uuid, World world) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            PlotSystem.getInstance().getLogger().warning("RegionManager for world " + world.getName() + " is null!");
            return;

        }

        //Check if the member is in the region.
        ProtectedRegion region = buildRegions.getRegion(regionName);

        if (region == null) {

            PlotSystem.getInstance().getLogger().warning("Region " + regionName + " does not exist!");
            return;

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

    public static void clearMembers(String regionName, World world) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            PlotSystem.getInstance().getLogger().warning("RegionManager for world " + world.getName() + " is null!");
            return;

        }

        ProtectedRegion region = buildRegions.getRegion(regionName);

        if (region == null) {

            PlotSystem.getInstance().getLogger().warning("Region " + regionName + " does not exist!");
            return;

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

    public static boolean delete(String regionName, World world) {

        //Get instance of WorldGuard.
        WorldGuard wg = WorldGuard.getInstance();

        //Get regions.
        RegionContainer container = wg.getPlatform().getRegionContainer();
        RegionManager buildRegions = container.get(BukkitAdapter.adapt(world));

        if (buildRegions == null) {

            PlotSystem.getInstance().getLogger().warning("RegionManager for world " + world.getName() + " is null!");
            return false;

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
}
