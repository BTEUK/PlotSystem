package me.bteuk.plotsystem.events;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
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

            String message = "&aYou have joined plot &3" + id;

            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Check if you have not already reached the maximum number of plots.
            if (plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

                message = "&aYou have reached the maximum number of plots.";

            } else {

                //Add the player to the database.
                plotSQL.update("INSERT INTO plot_members(id,uuid,is_owner,last_enter) VALUES(" + id + ",'" + uuid + "',0," + Time.currentTime() + ");");

                //Add the player to the worldguard region.
                WorldGuardFunctions.addMember(id, uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")));

            }

            if (p != null) {

                p.sendMessage(Utils.chat(message));

            } else {

                //Send a cross-server message.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "';");

            }
        }
    }
}
