package net.bteuk.plotsystem.events;

import net.bteuk.network.Network;
import net.bteuk.network.lib.dto.ChatMessage;
import net.bteuk.network.lib.utils.ChatUtils;
import net.bteuk.network.utils.enums.PlotStatus;
import net.bteuk.plotsystem.PlotSystem;
import net.bteuk.plotsystem.utils.PlotHelper;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

                //Set plot status to claimed.
                PlotHelper.updatePlotStatus(id, PlotStatus.CLAIMED);

                //Remove submitted plot entry.
                PlotSystem.getInstance().plotSQL.update("DELETE FROM plot_submissions WHERE id=" + id + ";");

                message = "&aRetracted submission for plot &3" + id;

            } else {

                //If plot is not submitted set the message accordingly.
                message = "&cPlot submission can not be retracted as it is not submitted.";

            }

            if (p != null) {

                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

            } else {

                //Send a cross-server message.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "');");

            }

            //Get number of submitted plots.
            int plot_count = PlotSystem.getInstance().plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

            //Send message to reviewers that a plot submission has been retracted.
            ChatMessage chatMessage = new ChatMessage("reviewer", "server",
                    ChatUtils.success("A submitted plot has been retracted, there " + (plot_count == 1 ? "is" : "are") + " %s submitted plots.", String.valueOf(plot_count))
            );
            Network.getInstance().getChat().sendSocketMesage(chatMessage);
        }
    }
}
