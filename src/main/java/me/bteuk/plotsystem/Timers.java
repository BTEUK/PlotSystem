package me.bteuk.plotsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.bteuk.plotsystem.events.EventManager;
import me.bteuk.plotsystem.sql.GlobalSQL;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Inactive;
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
    private final PlotSQL plotSQL;

    //Server events
    private HashMap<String, String> events;

    public Timers(PlotSystem instance, GlobalSQL globalSQL, PlotSQL plotSQL) {

        this.instance = instance;
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

        SERVER_NAME = PlotSystem.SERVER_NAME;

        events = new HashMap<>();

    }

    public void startTimers() {

        //1 tick timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for new server_events.
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server=" + SERVER_NAME + " AND type='plotsystem';")) {

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

        //1 hour timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Remove inactive plots.
            Inactive.cancelInactivePlots();

        }, 0L, 72000L);
    }
}