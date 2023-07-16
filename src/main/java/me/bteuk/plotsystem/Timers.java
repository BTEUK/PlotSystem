package me.bteuk.plotsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sk89q.worldguard.WorldGuard;
import me.bteuk.plotsystem.events.EventManager;
import me.bteuk.plotsystem.sql.GlobalSQL;

import me.bteuk.plotsystem.utils.Inactive;
import me.bteuk.plotsystem.utils.Outlines;
import me.bteuk.plotsystem.utils.User;

public class Timers {

    //Plugin
    private final PlotSystem instance;

    //Users
    private final ArrayList<User> users;

    //Server name
    private final String SERVER_NAME;

    //SQL
    private final GlobalSQL globalSQL;

    //Server events
    private HashMap<String, String> events;

    //Outlines.
    private final Outlines outlines;

    final WorldGuard wg;

    public Timers(PlotSystem instance, GlobalSQL globalSQL) {

        this.instance = instance;
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;

        SERVER_NAME = PlotSystem.SERVER_NAME;

        events = new HashMap<>();

        wg = WorldGuard.getInstance();

        outlines = instance.getOutlines();

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

                /*Check if the location of the player has changed by more than 50 blocks,
                    or if the player has switched world.
                   If either are true, recalculate the outlines.
                   Else try to update the existing outlines,
                    catch a nullpointerexception,
                    this implies that the player has no outlines
                    then also add the outlines anew.
                 */
                if (!u.player.getWorld().equals(u.lastLocation.getWorld())) {

                    outlines.addNearbyOutlines(u.player);
                    u.lastLocation = u.player.getLocation();

                } else if (u.player.getLocation().distance(u.lastLocation) >= 50) {

                    outlines.addNearbyOutlines(u.player);
                    u.lastLocation = u.player.getLocation();

                } else {

                    try {
                        outlines.refreshOutlinesForPlayer(u.player);
                    } catch (NullPointerException e) {
                        outlines.addNearbyOutlines(u.player);
                        u.lastLocation = u.player.getLocation();
                    }

                }


            }
        }, 0L, 20L);

        //1 hour timer.
        //Remove inactive plots.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {
            Inactive.cancelInactivePlots();
            Inactive.closeExpiredZones();
        }, 1200L, 72000L);
    }
}