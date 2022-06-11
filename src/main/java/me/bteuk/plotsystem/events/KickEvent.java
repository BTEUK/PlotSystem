package me.bteuk.plotsystem.events;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KickEvent {

    public static void event(String uuid, String[] event) {

        //Events for retracting
        if (event[1].equals("plot")) {

            GlobalSQL globalSQL = PlotSystem.getInstance().globalSQL;
            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get the player of member and owner if they exist.
            Player member = Bukkit.getPlayer(UUID.fromString(uuid));
            Player owner = Bukkit.getPlayer(UUID.fromString(plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + id + " AND is_owner=1")));

            String messageOwner = "&cYou have kicked &4" + globalSQL.getString("SELECT name FROM player_data WHERE uuid=" + uuid + ";") + " &cfrom plot &4" + id;
            String messageMember = "&aYou have been kicked from plot &4" + id;

            //Remove the player to the database.
            plotSQL.update("DELETE FROM plot_members WHERE id=" + id + " AND uuid=" + uuid + ";");

            //Remove the player to the worldguard region.
            WorldGuardFunctions.removeMember(id, uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")));

            //Send message to plot owner.
            if (owner != null) {

                owner.sendMessage(Utils.chat(messageOwner));

            } else {

                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES(" + plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + id + " AND is_owner=1") + "," + messageOwner);

            }

            //Send message to plot member.
            if (member != null) {

                member.sendMessage(Utils.chat(messageMember));

            } else {

                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES(" + uuid + "," + messageMember);

            }
        }
    }

}
