package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.User;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
			instance.getLogger().warning(Utils.chat("&cError: User " + e.getPlayer().getName() + " not found in the list of online users!" ));
		}

		//Get player instance.
		Player p = e.getPlayer();

		//If the player is in a review, cancel it.
		//TODO: Reviewing must only be cancelled if the player disconnects from the network.
		/*
		if (u.review != null) {

			WorldGuardFunctions.removeMember(u.review.plot, u.uuid);
			plotData.setStatus(u.review.plot, "submitted");
			u.review.editBook.unregister();
			u.player.getInventory().setItem(4, u.review.previousItem);

		}
		 */

		//Remove user from list
		instance.getUsers().remove(u);

	}

}
