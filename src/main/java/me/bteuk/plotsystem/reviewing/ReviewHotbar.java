package me.bteuk.plotsystem.reviewing;

import me.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;

public class ReviewHotbar implements Listener {

    Review review;

    public ReviewHotbar(PlotSystem plotSystem, Review review) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plotSystem);
        this.review = review;
    }

    /*

    Create listeners for inventory.

     */

    public void unregister() {
        PlayerEditBookEvent.getHandlerList().unregister(this);
    }
}
