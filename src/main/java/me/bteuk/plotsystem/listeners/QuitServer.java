package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;

import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
            instance.getLogger().warning("&cError: User " + e.getPlayer().getName() + " not found in the list of online users!");
        }

        //If the player is in a review, cancel it.
        if (u.review != null) {

            PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

            //Remove the reviewer from the plot.
            WorldGuardFunctions.removeMember(u.review.plot, u.uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + u.review.plot + ";")));

            //Set status back to submitted.
            plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + u.review.plot + ";");

            //Close review.
            u.review.closeReview();

        }

        //If the player has a claim or create gui delete it.
        if (u.claimGui != null) {
            u.claimGui.delete();
        }

        if (u.createGui != null) {
            u.createGui.delete();
        }

        //Remove user from list
        instance.removeUser(u);

    }

}
