package me.bteuk.plotsystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.plotsystem.sql.GlobalSQL;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

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
            if (globalSQL.hasRow("SELECT uuid FROM server_events WHERE server=" + SERVER_NAME + ";")) {

                //Get events for this server.
                events.clear();
                events = globalSQL.getEvents(SERVER_NAME, events);

                for (Map.Entry<String, String> entry : events.entrySet()) {

                    //Deal with events here.
                    //Get player for the event.
                    Player p = Bukkit.getPlayer(UUID.fromString(entry.getKey()));

                    //If the player is not null, get the user.
                    if (p != null) {

                        User u = instance.getUser(p);

                        //Split the event by word.
                        String[] aEvent = entry.getValue().split(" ");

                        //Start the execution process by looking at the event message structure.
                        if (aEvent[0].equals("teleport")) {

                            //Events for teleporting
                            if (aEvent[1].equals("plot")) {

                                //Convert the string id to int id.
                                int id = Integer.parseInt(aEvent[2]);

                                //Teleport to specific plot id.
                                //Get the server of the plot.
                                String server = plotSQL.getString("SELECT server FROM location_data WHERE name="
                                        + plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                        + ";");

                                //If the plot is on the current server teleport them directly.
                                //Else teleport them to the correct server and them teleport them to the plot.
                                if (server.equals(Network.SERVER_NAME)) {

                                    //Get world of plot.
                                    World world = Bukkit.getWorld(plotSQL.getString("SELECT world FROM location_data WHERE name="
                                            + plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                                            + ";"));

                                    //Get location of plot and teleport the player there.
                                    u.player.teleport(WorldGuardFunctions.getCurrentLocation(id, world));

                                } else {

                                    //Set the server join event.
                                    globalSQL.update("INSERT INTO join_events(uuid,event) VALUES("
                                            + u.player.getUniqueId()
                                            + "," + "teleport plot " + id + ");");

                                    //Teleport them to another server.
                                    u.player.closeInventory();
                                    ByteArrayDataOutput out = ByteStreams.newDataOutput();
                                    out.writeUTF("Connect");
                                    out.writeUTF(server);

                                }
                            }


                        }
                    }
                }
            }
        }, 0L, 1L);


        //1 second timer.
        instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, () -> {

            for (User u : users) {

                //Set the world of the player.
                u.world = u.player.getWorld();

            }

        }, 0L, 20L);
    }

}