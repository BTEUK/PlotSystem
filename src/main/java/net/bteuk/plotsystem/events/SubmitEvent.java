package net.bteuk.plotsystem.events;

import net.bteuk.network.Network;
import net.bteuk.network.utils.Time;
import net.bteuk.network.utils.Utils;
import net.bteuk.plotsystem.PlotSystem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SubmitEvent {

    public static void event(String uuid, String[] event) {

        //Events for submitting
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            //Convert the string id to int id.
            int id = Integer.parseInt(event[2]);

            //Check if the player can submit a plot at this point in time.
            long lCoolDown = PlotSystem.getInstance().getConfig().getInt("submit_cooldown") * 60L * 1000L;
            long lSubmit = PlotSystem.getInstance().globalSQL.getLong("SELECT last_submit FROM player_data WHERE uuid='" + uuid + "';");

            String message;
            boolean success = true;

            if (Time.currentTime() - lSubmit <= lCoolDown) {

                long lon_dif = lCoolDown - (Time.currentTime() - lSubmit);

                int sec = (int) ((lon_dif / 1000) % 60);
                int min = (int) ((lon_dif / 1000) / 60);

                String time;

                if (min == 0) {
                    time = sec + " second";
                } else {
                    if (sec == 0) {
                        time = min + " minute";
                    } else {
                        time = min + " minute and " + sec + " second";
                    }
                }

                message = "&cYou have a &4" + time + " &ccooldown before you can submit another plot.";
                success = false;

            } else {

                //Check if plot is claimed.
                if (PlotSystem.getInstance().plotSQL.hasRow("SELECT id FROM plot_data WHERE id=" + id + " AND status='claimed';")) {

                    //Set plot status to submitted.
                    PlotSystem.getInstance().plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + id + ";");

                    //Create new submitted plot key.
                    PlotSystem.getInstance().plotSQL.update("INSERT INTO plot_submissions(id,submit_time,last_query) VALUES(" + id + "," + Time.currentTime() + "," + Time.currentTime() + ");");

                    //Update last submit time in playerdata.
                    PlotSystem.getInstance().globalSQL.update("UPDATE player_data SET last_submit=" + Time.currentTime() + " WHERE uuid='" + uuid + "';");

                    message = "&aSubmitted plot &3" + id;

                } else {

                    //If plot is not claimed set the message accordingly.
                    message = "&cPlot can not be submitted";

                }
            }

            //Send message to player if they are on the server.
            if (p != null) {

                p.sendMessage(LegacyComponentSerializer.legacyAmpersand().deserialize(message));

            } else {

                //Send a cross-server message.
                PlotSystem.getInstance().globalSQL.update("INSERT INTO messages(recipient,message) VALUES('" + uuid + "','" + message + "');");

            }

            if (success) {
                //Get number of submitted plots.
                int plot_count = PlotSystem.getInstance().plotSQL.getInt("SELECT count(id) FROM plot_data WHERE status='submitted';");

                //Send message to reviewers that a plot has been submitted.
                if (plot_count == 1) {
                    Network.getInstance().chat.broadcastMessage(Utils.success("A plot has been submitted, there is ")
                            .append(Component.text(1, NamedTextColor.DARK_AQUA))
                            .append(Utils.success(" submitted plot.")), "uknet:reviewer");
                } else {
                    Network.getInstance().chat.broadcastMessage(Utils.success("A plot has been submitted, there are ")
                            .append(Component.text(plot_count, NamedTextColor.DARK_AQUA))
                            .append(Utils.success(" submitted plots.")), "uknet:reviewer");
                }
            }
        }
    }
}
