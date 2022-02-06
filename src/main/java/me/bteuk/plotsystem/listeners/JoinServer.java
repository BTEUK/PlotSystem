package me.bteuk.plotsystem.listeners;

import me.bteuk.plotsystem.navigation.SwitchServer;
import me.bteuk.plotsystem.sql.PlotSQL;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.tutorial.TutorialInfo;
import me.bteuk.plotsystem.utils.Inactive;
import me.bteuk.plotsystem.utils.User;

/*
This class will be a global class, used for all server types.
It will create the initial user class with basic information, such as uuid, name, player.
Additionally the tutorial data will be loaded to check whether the player needs to complete the tutorial first.
If this server does not have a tutorial, but it has not been completed, then the player will be sent to
an alternative server which does have a tutorial.
 */
public class JoinServer implements Listener {

	private PlotSQL plotSQL;
	public JoinServer(Main plugin, PlotSQL plotSQL) {

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plotSQL = plotSQL;

	}
	
	@EventHandler(priority = EventPriority.HIGH)
	public void joinEvent(PlayerJoinEvent e) {
		
		//Create instance of User and add it to list.
		User u = new User(e.getPlayer());
		Main.getInstance().getUsers().add(u);

		//If the player has not completed the tutorial and the server is for plots only
		//Send the player to a server with a tutorial, if one exists.
		if (!u.tutorial_complete && Main.PLOTS_ONLY) {

			if (plotSQL.tutorialExists()) {

				String server = plotSQL.getTutorialServer();

				u.player.sendMessage(Component.text("Teleporting to tutorial.", NamedTextColor.GREEN));
				SwitchServer.toServer(u, server);

			}

		}

	}
}
