package me.bteuk.plotsystem.events;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Event to allow adding/removing outlines for specific plots.
 */
public class OutlinesEvent {
    public static void event(String uuid, String[] event) {

        // Get the user.
        Player p = Bukkit.getPlayer(UUID.fromString(uuid));
        if (p == null) {
            Bukkit.getLogger().warning("Player " + uuid + " is not on the server the event was sent to!");
            return;
        }
        User user = PlotSystem.getInstance().getUser(p);
        if (user == null) {
            Bukkit.getLogger().warning("User " + p.getName() + " is not on the server the event was sent to!");
            return;
        }

        //Events adding/removing outlines for a specific plot.
        if (event[1].equals("toggle")) {// Check if the outlines are currently disabled.
            if (user.getSkipOutlines().contains(event[2])) {
                // Enable:
                // Remove the plot from the list of ignored outlines.
                user.getSkipOutlines().remove(event[2]);
                // Add the outlines back
                PlotSystem.getInstance().getOutlines().addPlotOutlineForPlayer(event[2], p);
                // Notify player.
                p.sendMessage(Utils.success("Enabled outlines for plot " + event[2]));
            } else {
                // Disable:
                // Add the plot to the list of ignored outlines.
                user.getSkipOutlines().add(event[2]);
                // Remove the outline.
                PlotSystem.getInstance().getOutlines().removePlotOutlineForPlayer(event[2], p);
                // Notify player.
                p.sendMessage(Utils.success("Disabled outlines for plot " + event[2]));
            }
        }
    }
}
