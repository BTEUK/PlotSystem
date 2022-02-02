package me.bteuk.plotsystem.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.mysql.HologramData;
import me.bteuk.plotsystem.mysql.HologramText;
import me.bteuk.plotsystem.utils.Holograms;

public class CustomHolo implements CommandExecutor {
	
	HologramData hologramData;
	HologramText hologramText;
	Holograms holograms;
	
	public CustomHolo(HologramData hologramData, HologramText hologramText, Holograms holograms) {
		this.hologramData = hologramData;
		this.hologramText = hologramText;
		this.holograms = holograms;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
				
		if (!(sender instanceof Player)) {
			
			sender.sendMessage(ChatColor.RED + "This command can only be run as a player ");
			return true;
			
		}
		
		Player p = (Player) sender;
		
		if (!p.hasPermission("publicbuilds.customholo")) {
			p.sendMessage(ChatColor.RED + "You do not have permission to use this command!");
			return true;
		}
		
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("reload")) {
				
				//Reload all holograms created by this plugin.
				holograms.reloadAll();
				p.sendMessage(ChatColor.GREEN + "Reloaded holograms!");
				
				return true;
			} else if (args[0].equalsIgnoreCase("help")) {
				help(p);
				return true;
			} else {
				p.sendMessage(ChatColor.GREEN + "/customholo help");
				return true;
			}
		}
		
		if (args.length < 2) {
			p.sendMessage(ChatColor.GREEN + "/customholo help");
			return true;
		}
		
		if (args[0].equalsIgnoreCase("create")) {
			
			//If a hologram with this name already exists, stop.
			if (hologramData.nameExists(args[1])) {
				p.sendMessage(ChatColor.RED + "A hologram with this name already exists!");
				return true;
			}
			
			Boolean visible = true;
			
			if (args.length >= 3) {
				if (args[2].equalsIgnoreCase("false")) {
					visible = false;
				}
			}
			
			//Add the hologram to the database, if it fails notify the player.
			if (hologramData.create(args[1], p.getLocation(), visible)) {
				p.sendMessage(ChatColor.GREEN + "Hologram " + args[1] + " successfully created at your position.");
				return true;
			} else {
				p.sendMessage(ChatColor.RED + "An error occured, please contact a server admin!");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("delete")) {
			
			//If a hologram with this name exists, delete it.
			if (hologramData.nameExists(args[1])) {
				hologramData.delete(args[1]);
				hologramText.deleteLines(args[1]);
				p.sendMessage(ChatColor.GREEN + "Hologram " + args[1] + " deleted!");
				return true; 
			} else {
				p.sendMessage(ChatColor.RED + "No hologram with this name exists!");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("movehere")) {
			
			//If a hologram with this name exists, move it to the player.
			if (hologramData.nameExists(args[1])) {
				hologramData.move(args[1], p.getLocation());
				p.sendMessage(ChatColor.GREEN + "Hologram " + args[1] + " moved to your location.");
				return true;
			} else {
				p.sendMessage(ChatColor.RED + "No hologram with this name exists!");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("togglevisibility")) {
			
			//If a hologram with this name exists, toggle the visibility.
			if (hologramData.nameExists(args[1])) {
				hologramData.toggleVisibility(args[1]);
				return true;
			} else {
				p.sendMessage(ChatColor.RED + "No hologram with this name exists!");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("setline")) {
			
			if (args.length < 4) {
				p.sendMessage(ChatColor.RED + "/customholo setline <name> <line> <text>");
				return true;
			}
			
			if (!hologramData.nameExists(args[1])) {
				p.sendMessage(ChatColor.RED + "There is no hologram with this name.");
				return true;
			}
			
			int line;
			try {
			   line = Integer.parseInt(args[2]);
			}
			catch (NumberFormatException e) {
			   p.sendMessage(ChatColor.RED + "/customholo setline <name> <line> <text>");
			   return true;
			}
			
			if (hologramText.lines(args[1]) + 1 < line) {
				p.sendMessage(ChatColor.RED + "Can't add line " + line + ", the hologram only has " + hologramText.lines(args[1]));
				return true;
			}
			
			String text = args[3];
			
			if (args.length > 4) {
				for (int i = 4; i < args.length; i++) {
					text = text + " " + args[i];
				}
			}
			
			if (hologramText.hasLine(args[1], line)) {
				if (hologramText.updateLine(args[1], line, text)) {
					p.sendMessage(ChatColor.GREEN + "Line " + line + " updated successfully for hologram " + args[1]);
					return true;
				} else {
					p.sendMessage(ChatColor.RED + "An error occured, please contact a server admin!");
					return true;
				}
			}
			
			if (hologramText.addLine(args[1], line, text)) {
				p.sendMessage(ChatColor.GREEN + "Line " + line + " added successfully to hologram " + args[1]);
				return true;
			} else {
				p.sendMessage(ChatColor.RED + "An error occured, please contact a server admin!");
				return true;
			}
			
		} else if (args[0].equalsIgnoreCase("removeline")) {
			
			if (!hologramData.nameExists(args[1])) {
				p.sendMessage(ChatColor.RED + "There is no hologram with this name.");
				return true;
			}
			
			if (hologramText.hasLine(args[1])) {
				hologramText.removeLine(args[1]);
				return true;
			} else {
				p.sendMessage(ChatColor.RED + "This hologram has no lines");
				return true;
			}
			
		}
		
		p.sendMessage(ChatColor.GREEN + "/customholo help");	
		return true;
		
	}

	
	private void help(Player p) {
		p.sendMessage(ChatColor.GREEN + "/customholo create <name> [true|false]");
		p.sendMessage(ChatColor.GREEN + "/customholo delete <name>");
		p.sendMessage(ChatColor.GREEN + "/customholo movehere <name>");
		p.sendMessage(ChatColor.GREEN + "/customholo togglevisibility <name>");
		p.sendMessage(ChatColor.GREEN + "/customholo setline <name> <line> <text>");
		p.sendMessage(ChatColor.GREEN + "/customholo reload");
	}
}
