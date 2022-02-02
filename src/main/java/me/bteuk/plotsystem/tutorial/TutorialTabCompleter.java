package me.bteuk.plotsystem.tutorial;

import java.util.Arrays;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class TutorialTabCompleter implements TabCompleter {

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String aslias, String[] args) {

		if (sender instanceof Player) {
			
			if (cmd.getName().equalsIgnoreCase("tutorial")) {

				List<String> list = Arrays.asList("tpll", "plot", "start", "optional");		

				return list;
				
			} else {return null;}
		} else {return null;}
	}

}
