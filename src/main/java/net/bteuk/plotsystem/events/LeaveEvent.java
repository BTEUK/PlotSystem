package net.bteuk.plotsystem.events;

import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import net.bteuk.plotsystem.exceptions.RegionNotFoundException;
import net.bteuk.plotsystem.utils.PlotHelper;
import net.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.UUID;

public class LeaveEvent {

    public static void event(String uuid, String[] event) {

        //Events for leaving
        if (event[1].equals("plot")) {

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get worlds of plot.
            World world = Bukkit.getWorld(PlotSystem.getInstance().plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";"));

            if (world == null) {

                //Send error to console.
                Bukkit.getLogger().severe("Plot leave event failed!");
                Bukkit.getLogger().severe("Event details:" + Arrays.toString(event));
                return;

            }

            //Remove member from plot.
            try {
                WorldGuardFunctions.removeMember(event[2], uuid, world);
            } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('&cAn error occurred while removing you from the plot, please contact an admin.');");
                e.printStackTrace();
            }

            //Remove members from plot in database.
            PlotSystem.getInstance().plotSQL.update("DELETE FROM plot_members WHERE id=" + id + " AND uuid='" + uuid + "';");

            //Send message to plot owner.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //If the player is on this server send them a message.
            if (p != null) {

                // Update the hologram since they are on the server.
                PlotHelper.updatePlotHologram(id);

                p.sendMessage(ChatUtils.success("You have left Plot ")
                        .append(Component.text(id, NamedTextColor.DARK_AQUA)));

            } else {

                //Add the message to the database so it can be sent wherever they are currently.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&cYou have left plot &4" + id + "');");

            }
        } else if (event[1].equals("zone")) {

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get worlds of plot.
            World world = Bukkit.getWorld(PlotSystem.getInstance().plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";"));

            if (world == null) {

                //Send error to console.
                Bukkit.getLogger().severe("Zone leave event failed!");
                Bukkit.getLogger().severe("Event details:" + Arrays.toString(event));
                return;

            }

            //Remove member from zone.
            try {
                WorldGuardFunctions.removeMember("z" + event[2], uuid, world);
            } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('&cAn error occurred while removing you from the zone, please contact an admin.');");
                e.printStackTrace();
            }

            //Remove members from zone in database.
            PlotSystem.getInstance().plotSQL.update("DELETE FROM zone_members WHERE id=" + id + " AND uuid='" + uuid + "';");

            //Send message to plot owner.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //If the player is on this server send them a message.
            if (p != null) {

                p.sendMessage(ChatUtils.success("You have left Zone ")
                        .append(Component.text(id, NamedTextColor.DARK_AQUA)));

            } else {

                //Add the message to the database so it can be sent wherever they are currently.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','&aYou have left Zone &3" + id + "');");

            }
        }
    }
}
