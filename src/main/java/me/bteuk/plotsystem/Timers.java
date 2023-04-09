package me.bteuk.plotsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import me.bteuk.plotsystem.events.EventManager;
import me.bteuk.plotsystem.sql.GlobalSQL;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Inactive;
import me.bteuk.plotsystem.utils.PlotOutline;
import me.bteuk.plotsystem.utils.User;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

public class Timers {

    //Plugin
    private final PlotSystem instance;

    //Users
    private final ArrayList<User> users;

    //Server name
    private final String SERVER_NAME;

    //SQL
    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    //Server events
    private HashMap<String, String> events;

    //Block data
    BlockData redConc = Material.RED_CONCRETE.createBlockData();
    BlockData yellowConc = Material.YELLOW_CONCRETE.createBlockData();
    BlockData limeConc = Material.LIME_CONCRETE.createBlockData();

    //Regions
    RegionManager regions;
    ProtectedRegion region;
    int plotID;
    int difficulty;
    PlotOutline plotOutline;

    WorldGuard wg;

    public Timers(PlotSystem instance, GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.instance = instance;
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

        SERVER_NAME = PlotSystem.SERVER_NAME;

        events = new HashMap<>();

        plotOutline = new PlotOutline();

        wg = WorldGuard.getInstance();

    }

    public void startTimers() {

        //1 tick timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for new server_events.
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server='" + SERVER_NAME + "' AND type='plotsystem';")) {

                //Get events for this server.
                events.clear();
                events = globalSQL.getEvents(SERVER_NAME, events);

                for (Map.Entry<String, String> entry : events.entrySet()) {

                    //Deal with events here.

                    //Split the event by word.
                    String[] aEvent = entry.getValue().split(" ");

                    //Send the event to the event handler.
                    EventManager.event(entry.getKey(), aEvent);

                }
            }
        }, 0L, 1L);

        //1 second timer.
        //Update plot and zone outlines.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            for (User u : users) {

                //Get regions.
                regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(u.player.getWorld()));

                if (regions == null) {return;}

                region = new ProtectedCuboidRegion("test",
                        BlockVector3.at(u.player.getLocation().getX() - 100, -60, u.player.getLocation().getZ() - 100),
                        BlockVector3.at(u.player.getLocation().getX() + 100, 320, u.player.getLocation().getZ() + 100));
                ApplicableRegionSet set = regions.getApplicableRegions(region);

                for (ProtectedRegion protectedRegion : set) {

                    plotID = tryParse(protectedRegion.getId());

                    //If plotID is 0, then it's a zone.
                    if (plotID == 0) {

                        plotOutline.createOutline(u.player, protectedRegion.getPoints(), Material.PURPLE_CONCRETE.createBlockData(), false);

                    } else {

                        //Get plot difficulty.
                        difficulty = plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";");

                        plotOutline.createOutline(u.player, protectedRegion.getPoints(), difficultyMaterial(difficulty), false);

                    }
                }
            }
        }, 0L, 20L);

        //1 hour timer.
        //Remove inactive plots.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {
            Inactive.cancelInactivePlots();
            Inactive.closeExpiredZones();
        }, 0L, 72000L);
    }

    //Returns the plot difficulty material.
    public BlockData difficultyMaterial(int difficulty) {

        return switch (difficulty) {
            case 1 -> limeConc;
            case 2 -> yellowConc;
            case 3 -> redConc;
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