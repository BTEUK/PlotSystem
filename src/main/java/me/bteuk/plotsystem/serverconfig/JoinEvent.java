package me.bteuk.plotsystem.serverconfig;

import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.bteuk.plotsystem.Main;

public class JoinEvent implements Listener {
	
	public JoinEvent(Main instance) {
		
		Bukkit.getServer().getPluginManager().registerEvents(this, instance);
		
	}
	
	public void ServerJoin(PlayerJoinEvent e) {
		
		//If the player has the permission to configure the server then open the gui.
		if (e.getPlayer().hasPermission("uknet.plots.configure")) {
			
			e.getPlayer().openInventory(SetupGui.Gui(e.getPlayer()));
			
		}		
	}
}
