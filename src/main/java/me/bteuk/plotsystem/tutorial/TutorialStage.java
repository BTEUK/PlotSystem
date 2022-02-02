package me.bteuk.plotsystem.tutorial;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.User;

public class TutorialStage implements CommandExecutor {
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (args.length != 1) {
			sender.sendMessage(error());
			return true;
		}

		if (Bukkit.getPlayer(args[0]) != null) {
			User u = Main.getInstance().getUser(Bukkit.getPlayer(args[0]));
			if (u.tutorial.complete) {
				sender.sendMessage(ChatColor.GREEN + u.name + " is not in the tutorial.");
				return true;
			} else {
				sender.sendMessage(ChatColor.GREEN + args[0] + " is in tutorial " + tutorialType(u.tutorial.tutorial_type));
				return true;
			}
		} else {
			sender.sendMessage(ChatColor.RED + "This player isn't online!");
			return true;
		}

	}
	
	private String error() {
		return (ChatColor.RED + "/tutorialstage <player>");
	}
	
	private String tutorialType(int type) {
		
		switch(type) {
		
		case 1:
			return "start";
		case 2:
			return "outlines";
		case 3:
			return "optional";
		case 4:
			return "worldedit";
		case 5:
			return "gep";
		case 6:
			return "roofs";
		case 7:
			return "details";
		case 8:
			return "texture";
		case 9:
			return "plot";				
		default:
			return "none";
		}
	}

}
