package me.bteuk.plotsystem.commands;

import me.bteuk.plotsystem.gui.CreatePlotGui;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;

public class CreatePlotCommand implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		
		//Check if the sender is a player
		if (!(sender instanceof Player)) {
			
			sender.sendMessage("This command can only be used by players!");
			return true;
			
		}
		
		//Get the user
		User u = Main.getInstance().getUser((Player) sender);
		
		//Check if the user has permission to use this command
		if (!u.player.hasPermission("uknet.plots.create")) {
			
			u.player.sendMessage(Utils.chat("&cYou do not have permission to use this command!"));
			return true;
			
		}
		
		//Check if the plot is valid
		if (u.plotFunctions.size() < 3) {
			
			u.player.sendMessage(Utils.chat("&cYou must select at least 3 points for a valid plot!"));
			return true;
			
		}
		
		//Open the plot creation menu
		u.plotFunctions.area();
		u.player.openInventory(CreatePlotGui.Gui(u));
		return true;
		
	}	
}
