package me.bteuk.plotsystem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.User;

public class Zone implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		if (!(sender instanceof Player)) {
			
			sender.sendMessage(ChatColor.RED + "This command can only be executed by a player!");
			return true;
			
		}
		
		Player p = (Player) sender;
		User u = Main.getInstance().getUser(p);
		
		if (args.length == 0) {
			help(p);
		}
		
		if (args[0].equalsIgnoreCase("create")) {
			
			//Paused to focus on tutorials.
			u.plotFunctions.size();
			
		} else if (args[0].equalsIgnoreCase("open")) {
			
		} else if (args[0].equalsIgnoreCase("close")) {
			
		} else if (args[0].equalsIgnoreCase("delete")) {
			
		} else if (args[0].equalsIgnoreCase("modify")) {
			
		}
		
		
		return true;
	}
	
	private void help(Player p) {
		
	}

}
