package me.bteuk.plotsystem.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.md_5.bungee.api.ChatColor;

public class QuitServer implements Listener {

	TutorialData tutorialData;
	PlayerData playerData;
	PlotData plotData;
	
	public QuitServer(Main plugin, TutorialData tutorialData, PlayerData playerData, PlotData plotData) {

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.tutorialData = tutorialData;
		this.playerData = playerData;
		this.plotData = plotData;

	}

	@EventHandler
	public void quitEvent(PlayerQuitEvent e) {

		//Get instance of plugin.
		Main instance = Main.getInstance();

		//Get user from the list.
		User u = instance.getUser(e.getPlayer());

		//If no user was found print error in console.
		if (u == null) {
			Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "Error: User " + e.getPlayer().getName() + " not found in the list of online users!" );
		}
		
		//Set building time
		/*if (Main.POINTS_ENABLED) {
			me.bteuk.btepoints.utils.PlayerData.setBuildTime(u.uuid, u.buildingTime);
		}*/

		//Get player instance.
		Player p = e.getPlayer();

		//Set tutorial stage in PlayerData
		tutorialData.updateValues(u);

		//Update the last online time of player.
		playerData.updateTime(p.getUniqueId().toString());

		//If the player is in a review, cancel it.
		if (u.review != null) {

			WorldGuardFunctions.removeMember(u.review.plot, u.uuid);
			plotData.setStatus(u.review.plot, "submitted");
			u.review.editBook.unregister();
			u.player.getInventory().setItem(4, u.review.previousItem);

		}

		//Remove user from list
		instance.getUsers().remove(u);

	}

}
