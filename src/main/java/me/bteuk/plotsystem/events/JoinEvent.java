package me.bteuk.plotsystem.events;

import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class JoinEvent {

    public static void event(String uuid, String[] event) {

        //Events for retracting
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            String message = "&aYou have joined Plot &3" + id;

            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Check if you have not already reached the maximum number of plots.
            if (plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

                message = "&aYou have reached the maximum number of plots.";

            } else {

                //Add the player to the database.
                plotSQL.update("INSERT INTO plot_members(id,uuid,is_owner,last_enter) VALUES(" + id + ",'" + uuid + "',0," + Time.currentTime() + ");");

                //Add the player to the worldguard region.
                try {
                    WorldGuardFunctions.addMember(String.valueOf(id), uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")));
                } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                    if (p != null) {

                        p.sendMessage(Utils.error("An error occurred while adding you to the plot, please contact an admin."));

                    } else {

                        //Send a cross-server message.
                        PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + "&cAn error occurred while adding you to the plot, please contact an admin." + "');");

                    }
                    e.printStackTrace();
                    return;
                }

                //Send a message to the plot owner.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" +
                        plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + id + " AND is_owner=1;") + "','&3" +
                        PlotSystem.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " &ahas joined your plot &3" + id + "');");

            }

            if (p != null) {

                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

            } else {

                //Send a cross-server message.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "');");

            }
        } else if (event[1].equals("zone")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            String message = "&aYou have joined Zone &3" + id;

            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Add the player to the database.
            plotSQL.update("INSERT INTO zone_members(id,uuid,is_owner) VALUES(" + id + ",'" + uuid + "',0);");

            //Add the player to the worldguard region.
            try {
                WorldGuardFunctions.addMember("z" + id, uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";")));
            } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                if (p != null) {

                    p.sendMessage(Utils.error("An error occurred while adding you to the zone, please contact an admin."));

                } else {

                    //Send a cross-server message.
                    PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + "&cAn error occurred while adding you to the plot, please contact an admin." + "');");

                }
                e.printStackTrace();
                return;
            }

            //Send a message to the zone owner.
            PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" +
                    plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + id + " AND is_owner=1;") + "','&3" +
                    PlotSystem.getInstance().globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " &ahas joined your Zone &3" + id + "');");

            if (p != null) {

                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

            } else {

                //Send a cross-server message.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "');");

            }
        }
    }
}
