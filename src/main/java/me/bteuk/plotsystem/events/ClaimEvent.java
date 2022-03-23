package me.bteuk.plotsystem.events;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ClaimEvent {

    public static void event(String uuid, String[] event) {

        //Events for claiming
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(uuid);

            if (p == null) {

                Bukkit.getLogger().warning(Utils.chat("Player " + uuid + " is not on the server the event was sent to!"));
                return;

            }

            User u = PlotSystem.getInstance().getUser(p);

            //If the player is not in a plot tell them.
            if (u.inPlot == 0) {

                p.sendMessage(Utils.chat("&cYou are not in a plot!"));
                return;

            }

            //If the plot is already claimed tell them.
            //If they are the owner or a member tell them.
            if (u.plotOwner) {

                p.sendMessage(Utils.chat("&cYou are already the owner of this plot!"));
                return;

            } else if (u.plotMember) {

                p.sendMessage(Utils.chat("&cYou are already a member of this plot!"));
                return;

            } else if (u.isClaimed) {

                p.sendMessage(Utils.chat("&cThis plot is already claimed!"));
                return;

            }

            //Claim the plot.
            //Add user to worldguard region.
            WorldGuardFunctions.addMember(u.inPlot, u.uuid, u.player.getWorld());

            //Set plot to claimed in database.
            u.plotSQL.update("UDPATE plot_data SET status='claimed' WHERE id=" + u.inPlot + ";");

            //Add user as plot owner in database.
            u.plotSQL.update("INSERT INTO plot_members(id,uuid,is_owner,last_enter) VALUES(" + u.inPlot + "," + u.uuid + ",1," + Time.currentTime() + ");");

            //Send plot claimed message.
            u.player.sendMessage(Utils.chat("&aYou have claimed plot " + u.inPlot + ", good luck building!"));

        }
    }
}
