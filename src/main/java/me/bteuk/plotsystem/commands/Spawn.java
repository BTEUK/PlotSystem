package me.bteuk.plotsystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.Main;

public class Spawn implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] arg3) {
		
		if (sender instanceof Player) {
			
			((Player) sender).teleport(Main.spawn);
			
		} else {
			sender.sendMessage("This command can only be run by a player!");
		}
		
		return true;
	}

}
