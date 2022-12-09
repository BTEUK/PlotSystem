package me.bteuk.plotsystem.events;

import me.bteuk.network.Network;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class RetractEvent {

    public static void event(String uuid, String[] event) {

        //Events for retracting
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            String message;

            //Check if plot is submitted.
            if (PlotSystem.getInstance().plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + id + " AND status='submitted';")) {

                //Set plot status to submitted.
                PlotSystem.getInstance().plotSQL.update("UPDATE plot_data SET status='claimed' WHERE id=" + id + ";");

                message = Utils.chat("&aRetracted submission for plot &3" + id);

            } else {

                //If plot is not submitted set the message accordingly.
                message = "&cPlot submission can not be retracted as it is not submitted.";

            }

            if (p != null) {

                p.sendMessage(Utils.chat(message));

            } else {

                //Send a cross-server message.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "';");

            }

            //Get number of submitted plots.
            int plot_count = PlotSystem.getInstance().plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

            //Send message to reviewers that a plot submission has been retracted.
            if (plot_count == 1) {
                Network.getInstance().chat.broadcastMessage("&aA submitted plot has been retracted, there is 1 submitted plot.", "uknet:reviewer");
            } else {
                Network.getInstance().chat.broadcastMessage("&aA submitted plot has been retracted, there are " + plot_count + " submitted plots.", "uknet:reviewer");
            }
        }
    }
}
