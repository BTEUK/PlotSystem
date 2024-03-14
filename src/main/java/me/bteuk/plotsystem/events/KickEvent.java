package me.bteuk.plotsystem.events;

import me.bteuk.network.Network;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class KickEvent {

    public static void event(String uuid, String[] event) {

        //Events for retracting
        if (event[1].equals("plot")) {

            GlobalSQL globalSQL = Network.getInstance().getGlobalSQL();
            PlotSQL plotSQL = Network.getInstance().getPlotSQL();

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get the player of member and owner if they exist.
            Player member = Bukkit.getPlayer(UUID.fromString(uuid));
            Player owner = Bukkit.getPlayer(UUID.fromString(plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + id + " AND is_owner=1")));

            String messageOwner = "&cYou have kicked &4" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " &cfrom Plot &4" + id;
            String messageMember = "&aYou have been kicked from Plot &3" + id;

            //Remove the player to the database.
            plotSQL.update("DELETE FROM plot_members WHERE id=" + id + " AND uuid='" + uuid + "';");

            //Remove the player to the worldguard region.
            try {
                WorldGuardFunctions.removeMember(String.valueOf(id), uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + id + ";")));
            } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES('&cAn error occurred while trying to kick the user from the plot, please notify an admin.','" + messageOwner + "');");
                e.printStackTrace();
            }

            //Send message to plot owner.
            if (owner != null) {

                owner.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(messageOwner));

            } else {

                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + id + " AND is_owner=1;") + "','" + messageOwner + "');");

            }

            //Send message to plot member.
            if (member != null) {

                member.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(messageMember));

            } else {

                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + messageMember + "');");

            }
        } else if (event[1].equals("zone")) {

            GlobalSQL globalSQL = Network.getInstance().getGlobalSQL();
            PlotSQL plotSQL = Network.getInstance().getPlotSQL();

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Get the player of member and owner if they exist.
            Player member = Bukkit.getPlayer(UUID.fromString(uuid));
            Player owner = Bukkit.getPlayer(UUID.fromString(plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + id + " AND is_owner=1")));

            String messageOwner = "&cYou have kicked &4" + globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + uuid + "';") + " &cfrom Zone &4" + id;
            String messageMember = "&aYou have been kicked from Zone &3" + id;

            //Remove the player to the database.
            plotSQL.update("DELETE FROM zone_members WHERE id=" + id + " AND uuid='" + uuid + "';");

            //Remove the player to the worldguard region.
            try {
                WorldGuardFunctions.removeMember("z" + event[2], uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM zones WHERE id=" + id + ";")));
            } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES('&cAn error occurred while trying to kick the user from the zone, please notify an admin.','" + messageOwner + "');");
                e.printStackTrace();
            }

            //Send message to plot owner.
            if (owner != null) {

                owner.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(messageOwner));

            } else {

                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + plotSQL.getString("SELECT uuid FROM zone_members WHERE id=" + id + " AND is_owner=1;") + "','" + messageOwner + "');");

            }

            //Send message to plot member.
            if (member != null) {

                member.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(messageMember));

            } else {

                //Send a cross-server message.
                globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + messageMember + "');");

            }
        }
    }

}
