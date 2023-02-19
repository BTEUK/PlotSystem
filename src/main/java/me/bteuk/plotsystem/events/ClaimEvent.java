package me.bteuk.plotsystem.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.ClaimGui;
import me.bteuk.plotsystem.utils.PlotValues;
import me.bteuk.plotsystem.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ClaimEvent {

    public static void event(String uuid, String[] event) {

        //Events for claiming
        if (event[1].equals("plot")) {

            //Get the user.
            Player p = Bukkit.getPlayer(UUID.fromString(uuid));

            if (p == null) {

                Bukkit.getLogger().warning("Player " + uuid + " is not on the server the event was sent to!");
                return;

            }

            User u = PlotSystem.getInstance().getUser(p);

            //If the player is not in a plot tell them.
            if (u.inPlot == 0) {

                p.sendMessage(Utils.error("You are not in a plot!"));
                return;

            }

            //If the plot is already claimed tell them.
            //If they are the owner or a member tell them.
            if (u.plotOwner) {

                p.sendMessage(Utils.error("You are already the owner of this plot!"));
                return;

            } else if (u.plotMember) {

                p.sendMessage(Utils.error("You are already a member of this plot!"));
                return;

            } else if (u.isClaimed) {

                p.sendMessage(Utils.error("This plot is already claimed!"));
                return;

            }

            //Check if you do not already have the maximum number of plots.
            if (u.plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

                p.sendMessage(Utils.error("You have reached the maximum number of plots."));
                return;

            }

            //Open the claim gui.
            NetworkUser user = Network.getInstance().getUser(u.player);

            //Check if the player has permission to claim a plot of this difficulty.
            if (!user.player.hasPermission("uknet.plots.claim.all")) {
                switch (u.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + u.inPlot + ";")) {

                    case 1 -> {
                        if (!user.player.hasPermission("uknet.plots.claim.easy")) {
                            user.player.sendMessage(Utils.error("You do not have permission to claim an &4Easy &cplot."));
                            return;
                        }
                    }

                    case 2 -> {
                        if (!user.player.hasPermission("uknet.plots.claim.normal")) {
                            user.player.sendMessage(Utils.error("You do not have permission to claim a &4Normal &cplot."));
                            return;
                        }
                    }

                    case 3 -> {
                        if (!user.player.hasPermission("uknet.plots.claim.hard")) {
                            user.player.sendMessage(Utils.error("You do not have permission to claim a &4Hard &cplot."));
                            return;
                        }
                    }
                }
            }

            u.player.closeInventory();
            u.claimGui = new ClaimGui(u);
            u.claimGui.open(user);

        }
    }
}
