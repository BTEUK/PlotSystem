package me.bteuk.plotsystem.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.utils.User;

public class OpenGui implements CommandExecutor {

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("&cYou cannot open the gui!");
			return true;
		}

		Player p = (Player) sender;
		User u = PlotSystem.getInstance().getUser(p);
		
		p.openInventory(MainGui.GUI(u));
		
		return true;

	}
}
