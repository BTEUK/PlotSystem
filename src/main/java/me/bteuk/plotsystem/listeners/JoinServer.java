package me.bteuk.plotsystem.listeners;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bteuk.network.Network;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.bteuk.plotsystem.utils.User;

/*
This class will be a global class, used for all server types.
It will create the initial user class with basic information, such as uuid, name, player.
Additionally, the tutorial data will be loaded to check whether the player needs to complete the tutorial first.
If this server does not have a tutorial, but it has not been completed, then the player will be sent to
an alternative server which does have a tutorial.
 */
public class JoinServer implements Listener {

    private final GlobalSQL globalSQL;
    private final PlotSQL plotSQL;

    public JoinServer(PlotSystem plugin, GlobalSQL globalSQL, PlotSQL plotSQL) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void joinEvent(PlayerJoinEvent e) {

        //Create instance of User and add it to list.
        User u = new User(e.getPlayer(), globalSQL, plotSQL);
        PlotSystem.getInstance().getUsers().add(u);

        //If the player has a join event, execute it.
        if (globalSQL.hasRow("SELECT uuid FROM join_events WHERE uuid=?;")) {

            //Get the event from the database.
            String event = globalSQL.getString("SELECT event FROM join_events WHERE uuid=?");

            //Split the event by word.
            String[] aEvent = event.split(" ");

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

            //Clear the events.
            globalSQL.update("DELETE FROM join_events WHERE uuid=?;");

        }


    }
}
