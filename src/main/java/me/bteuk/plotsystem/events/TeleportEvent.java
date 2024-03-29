package me.bteuk.plotsystem.events;

import me.bteuk.network.events.EventManager;
import me.bteuk.network.utils.SwitchServer;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.UUID;

import static me.bteuk.network.utils.Constants.SERVER_NAME;

public class TeleportEvent {

    public static void event(String uuid, String[] event) {

        //Events for teleporting
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            if (p == null) {

                //Send warning to console if player can't be found.
                Bukkit.getLogger().warning(("Attempting to teleport player with uuid " + uuid + " but they are not on this server."));
                return;

            }

            User u = PlotSystem.getInstance().getUser(p);

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Teleport to specific plot id.
            //Get the server of the plot.
            String server = u.plotSQL.getString("SELECT server FROM location_data WHERE name='"
                    + u.plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")
                    + "';");

            //If the plot is on the current server teleport them directly.
            //Else teleport them to the correct server and them teleport them to the plot.
            if (server.equals(SERVER_NAME)) {

                //Get world of plot.
                World world = Bukkit.getWorld(u.plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";"));

                //Get location of plot and teleport the player there.
                try {
                    Location l = WorldGuardFunctions.getCurrentLocation(event[2], world);
                    u.player.teleport(l);
                } catch (RegionNotFoundException | RegionManagerNotFoundException e) {
                    p.sendMessage(Utils.error("You could not be teleported to the plot, please notify an admin."));
                    e.printStackTrace();
                }

            } else {

                //Set the server join event.
                EventManager.createJoinEvent(u.player.getUniqueId().toString(), "plotsystem", "teleport plot" + id);

                //Teleport them to another server.
                SwitchServer.switchServer(u.player, server);

            }
        } else if (event[1].equals("zone")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            if (p == null) {

                //Send warning to console if player can't be found.
                Bukkit.getLogger().warning(("Attempting to teleport player with uuid " + uuid + " but they are not on this server."));
                return;

            }

            User u = PlotSystem.getInstance().getUser(p);

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);
            String zoneName = "z" + event[2];

            //Teleport to specific zone id.
            //Get the server of the zone.
            String server = u.plotSQL.getString("SELECT server FROM location_data WHERE name='"
                    + u.plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";")
                    + "';");

            //If the zone is on the current server teleport them directly.
            //Else teleport them to the correct server and them teleport them to the zone.
            if (server.equals(SERVER_NAME)) {

                //Get world of zone.
                World world = Bukkit.getWorld(u.plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";"));

                //Get location of zone and teleport the player there.
                try {
                    Location l = WorldGuardFunctions.getCurrentLocation(zoneName, world);
                    u.player.teleport(l);
                } catch (RegionNotFoundException | RegionManagerNotFoundException e) {
                    p.sendMessage(Utils.error("You could not be teleported to the zone, please notify an admin."));
                    e.printStackTrace();
                }

            } else {

                //Set the server join event.
                EventManager.createJoinEvent(u.player.getUniqueId().toString(), "plotsystem", "teleport zone" + id);

                //Teleport them to another server.
                SwitchServer.switchServer(u.player, server);

            }
        }

    }

}
