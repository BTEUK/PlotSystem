package me.bteuk.plotsystem.listeners;

import me.bteuk.network.Network;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import me.bteuk.plotsystem.exceptions.RegionNotFoundException;
import me.bteuk.plotsystem.utils.User;

import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static me.bteuk.plotsystem.PlotSystem.LOGGER;

public class QuitServer implements Listener {

    public QuitServer(PlotSystem plugin) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void quitEvent(PlayerQuitEvent e) {

        //Get instance of plugin.
        PlotSystem instance = PlotSystem.getInstance();

        //Get user from the list.
        User u = instance.getUser(e.getPlayer());

        //If no user was found print error in console.
        if (u == null) {
            LOGGER.warning("Error: User " + e.getPlayer().getName() + " not found in the list of online users!");
            return;
        }

        //If the player is in a review, cancel it.
        if (u.review != null) {

            PlotSQL plotSQL = Network.getInstance().getPlotSQL();

            //Remove the reviewer from the plot.
            try {
                WorldGuardFunctions.removeMember(String.valueOf(u.review.plot), u.uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + u.review.plot + ";")));
            } catch (RegionManagerNotFoundException | RegionNotFoundException ex) {
                ex.printStackTrace();
            }

            //Set status back to submitted.
            plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + u.review.plot + ";");

            //Close review.
            u.review.closeReview();

        }

        //If the player has a claim or create gui delete it.
        if (u.claimGui != null) {
            u.claimGui.delete();
        }

        if (u.createPlotGui != null) {
            u.createPlotGui.delete();
        }

        if (u.createZoneGui != null) {
            u.createZoneGui.delete();
        }

        //Remove player from outlines.
        PlotSystem.getInstance().getOutlines().removePlayer(e.getPlayer());

        //Remove user from list
        instance.removeUser(u);

    }

}
