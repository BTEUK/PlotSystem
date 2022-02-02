package me.bteuk.plotsystem.tutorial;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.User;

public class TutorialCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

		if (!(sender instanceof Player)) {
			sender.sendMessage("&cThis command can only be run as a player!");
			return true;
		}

		Player p = (Player) sender;

		if (args.length != 1) {
			p.sendMessage(error());
			return true;
		}
		
		User u = Main.getInstance().getUser(p);

		if (args[0].equalsIgnoreCase("tpll")) {
			u.tutorial.complete = false;
			u.tutorial.tutorial_type = 2;
			u.tutorial.tutorial_stage = 1;
			u.tutorial.continueTutorial();
		} else if (args[0].equalsIgnoreCase("plot")) {
			u.tutorial.complete = false;
			u.tutorial.tutorial_type = 9;
			u.tutorial.tutorial_stage = 1;
			u.tutorial.continueTutorial();
		} else if (args[0].equalsIgnoreCase("start")) {
			u.tutorial.complete = false;
			u.tutorial.tutorial_type = 1;
			u.tutorial.tutorial_stage = 1;
			u.tutorial.continueTutorial();
		} else if (args[0].equalsIgnoreCase("optional")) {
			u.tutorial.complete = false;
			u.tutorial.tutorial_type = 3;
			u.tutorial.tutorial_stage = 1;
			u.tutorial.continueTutorial();
		} else {
			p.sendMessage(error());
		}

		return true;

	}
	
	private String error() {
		return (ChatColor.RED + "/tutorial tpll,plot,start,optional");
	}

}
