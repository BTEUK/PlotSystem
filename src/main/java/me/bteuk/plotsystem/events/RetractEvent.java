package me.bteuk.plotsystem.events;

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

                message = Utils.chat("&aRetracted submission for plot " + id + ".");

            } else {

                //If plot is not submitted set the message accordingly.
                message = Utils.chat("&cPlot submission can not be retracted as it is not currently submitted.");

            }

            if (p != null) {

                p.sendMessage(message);

            }
        }
    }
}
