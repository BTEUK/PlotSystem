package me.bteuk.plotsystem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.utils.Leaderboard;
import me.bteuk.plotsystem.utils.User;

public class BuildingPoints implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("&cYou cannot use this command!");
			return true;
		}
		
		User u = Main.getInstance().getUser((Player) sender);

		if (args.length == 0) {

			Leaderboard.printLeaderboard(u);
			return true;
		}

		if (args.length != 1) {
			u.player.sendMessage(ChatColor.RED + "/points top/<name>");
			return true;
		}

		if (args[0].equalsIgnoreCase("top")) {

			Leaderboard.printLeaderboardTop(u);
			return true;
		}
		
		PlayerData playerData = Main.getInstance().playerData;
		
		String uuid = playerData.getUUID(args[0]);
		
		if (uuid == null) {
			
			u.player.sendMessage(ChatColor.RED + args[0] +  " has not connected to this server!");
			return true;
			
		}
		
		Leaderboard.printLeaderboardElse(u, uuid, playerData.getName(uuid));
		return true;
		
	}

}
