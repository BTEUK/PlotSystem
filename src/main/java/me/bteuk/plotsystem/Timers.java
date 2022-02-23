package me.bteuk.plotsystem;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.bteuk.plotsystem.sql.GlobalSQL;
import org.bukkit.ChatColor;

import me.bteuk.plotsystem.utils.User;

public class Timers {

    //Plugin
    private Main instance;

    //Users
    private ArrayList<User> users;

    //Server name
    private String SERVER_NAME;

    //SQL
    private GlobalSQL globalSQL;

    //Server events
    private HashMap<String, String> events;

    public Timers(Main instance, GlobalSQL globalSQL) {

        this.instance = instance;
        this.users = instance.getUsers();

        this.globalSQL = globalSQL;

        SERVER_NAME = Main.SERVER_NAME;

        events = new HashMap<>();

    }

    public void startTimers() {

        //1 tick timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            //Check for new server_events.
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server=" + SERVER_NAME + ";")) {

                //Get events for this server.
                events = globalSQL.getEvents(SERVER_NAME);

                for (Map.Entry<String, String> entry : events.entrySet()) {

                    //Deal with events here.

                }

            }


        }, 0L, 1L);


        //1 second timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
            public void run() {

                for (User u : users) {

                    //Set the world of the player.
                    u.world = u.player.getWorld();

                }

            }
        }, 0L, 20L);
    }

}