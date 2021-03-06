package me.bteuk.plotsystem.events;

import me.bteuk.network.Network;
import me.bteuk.network.utils.NetworkUser;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.ClaimGui;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
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

            //Check if you do not already have the maximum number of plots.
            if (u.plotSQL.getInt("SELECT count(id) FROM plot_members WHERE uuid='" + uuid + "';") >= PlotSystem.getInstance().getConfig().getInt("plot_maximum")) {

                p.sendMessage(Utils.chat("&aYou have reached the maximum number of plots."));
                return;

            }

            //Check if the player is allowed to claim this plot.
            int difficulty = u.plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + u.inPlot + ";");

            //Check by difficulty, with the required role.
            if (difficulty == 3 && !(u.player.hasPermission("group.jrbuilder"))) {

                p.sendMessage(Utils.chat("&cYou must be at least Jr.Builder or higher to claim a 'hard difficulty' plot."));
                return;

            } else if (difficulty == 2 && !(u.player.hasPermission("group.apprentice"))) {

                p.sendMessage(Utils.chat("&cYou must be at least Apprentice or higher to claim a 'medium difficulty' plot."));
                return;

            } else if (difficulty == 1 && !(u.player.hasPermission("group.applicant"))) {

                p.sendMessage(Utils.chat("&cYou must complete the tutorial to claim a plot."));
                return;

            }

            //Open the claim gui.
            NetworkUser user = Network.getInstance().getUser(u.player);
            u.player.closeInventory();
            u.claimGui = new ClaimGui(u);
            u.claimGui.open(user);

        }
    }
}
