package me.bteuk.plotsystem.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.utils.User;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class ClaimEnter implements Listener{

	public WorldGuardPlugin worldGuardPlugin;
	PlotData plotData;
	PlayerData playerData;

	public  ClaimEnter(Main plugin, PlotData plotData, PlayerData playerData) {

		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		this.plotData = plotData;
		this.playerData = playerData;
	}

	@EventHandler
	public void joinEvent(PlayerJoinEvent e) {

		User u = Main.getInstance().getUser(e.getPlayer());
		checkRegion(u);

	}

	@EventHandler
	public void moveEvent(PlayerMoveEvent e) {
		User u = Main.getInstance().getUser(e.getPlayer());
		if (e.getPlayer().getWorld().getName().equals(Main.getInstance().getConfig().getString("worlds.build"))){
			checkRegion(u);
		}
	}




	public void checkRegion(User u) {

		Location l = u.player.getLocation();

		RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
		ApplicableRegionSet applicableRegionSet = query.getApplicableRegions(BukkitAdapter.adapt(l));

		for(ProtectedRegion regions: applicableRegionSet) {
			if(regions.contains(BlockVector3.at(l.getX(), l.getY(), l.getZ()))) {
				try {

					String owners = regions.getOwners().toPlayersString();
					owners = owners.replace("uuid:", "");

					int plot = tryParse(regions.getId());

					if (plot == 0) {continue;}

					if (u.inPlot != plot) {

						u.inPlot = plot;
						u.plotOwner = owners;
						u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "You have entered " + playerData.getName(u.plotOwner) + "'s plot!"));
					}

					if (u.uuid.equals(owners)) {

						plotData.setLastVisit(u.inPlot);

					}


				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		if (applicableRegionSet.size() < 2 && u.inPlot != 0) {

			u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(ChatColor.GREEN + "You have left " + playerData.getName(u.plotOwner) + "'s plot!"));
			u.inPlot = 0;

		}
	}

	public static int tryParse(String text) {
		try {
			return Integer.parseInt(text);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}