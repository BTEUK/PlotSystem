package me.bteuk.plotsystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TutorialHelp implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("&cThis command can only run as a player!");
			return true;
		}

		Player p = (Player) sender;

		p.sendMessage("Tutorial Walkthrough:");
		p.sendMessage("https://youtu.be/vQ2jfxhI6B8");
		
		return true;
		
	}

}
