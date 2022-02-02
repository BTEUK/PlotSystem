package me.bteuk.plotsystem.tutorial;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.protection.regions.ProtectedPolygonalRegion;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.TutorialData;
import me.bteuk.plotsystem.utils.Holograms;
import me.bteuk.plotsystem.utils.User;

public class Tutorial {

	public int tutorial_type;
	public int tutorial_stage;

	public boolean first_time;
	public boolean complete;

	public boolean corner_1, corner_2, corner_3, corner_4;
	public boolean line_1, line_2, line_3, line_4;
	public int corner_sum, line_sum;
	
	private User u;
	private Holograms holograms;

	public Tutorial(User u) {

		this.u = u;
		holograms = Main.getInstance().getHolograms();
		
		TutorialData tutorialData = Main.getInstance().tutorialData;

		tutorial_type = tutorialData.getType(u.uuid);
		if (tutorial_type < 1) {
			tutorial_type = 1;
		}
		tutorial_stage = tutorialData.getStage(u.uuid);

		first_time = tutorialData.getTime(u.uuid);
		complete = false;

	}

	public Tutorial(User u, boolean complete) {

		this.u = u;
		holograms = Main.getInstance().getHolograms();
		
		tutorial_type = 10;
		tutorial_stage = 0;

		first_time = false;
		this.complete = complete;
	}

	public void continueTutorial() {

		if (tutorial_type == 1) {
			u.player.teleport(TutorialConstants.TUTORIAL_1_TELEPORT);
		} else if (tutorial_type == 2) {

			if (tutorial_stage == 1) {						
				startStage2_1();
			} else if (tutorial_stage == 2) {
				startStage2_2();
			} else if (tutorial_stage == 3) {
				startStage2_3();
			}
			
		} else if (tutorial_type == 3) {
			u.player.teleport(TutorialConstants.TUTORIAL_3_START);
		} else if (tutorial_type == 4) {

		} else if (tutorial_type == 5) {

		} else if (tutorial_type == 6) {

		} else if (tutorial_type == 7) {

		} else if (tutorial_type == 8) {

		} else if (tutorial_type == 9) {
			u.player.teleport(TutorialConstants.TUTORIAL_9_START);
			startStage9();
		} 


	}
	
	public void setHologramVisibility() {
		
		if (tutorial_type == 2) {
			
			if (tutorial_stage == 1) {
				holograms.hideHologram(u.player, "tutorial2_2");
				holograms.showHologram(u.player, "tutorial2_1");
			} else if (tutorial_stage == 2) {
				holograms.hideHologram(u.player, "tutorial2_1");
				holograms.showHologram(u.player, "tutorial2_2");
			} else if (tutorial_stage == 3) {
				holograms.hideHologram(u.player, "tutorial2_2");
			}
						
		}		
	}
	
	public void removeHologramVisibility() {
		holograms.hideHologram(u.player, "tutorial2_1");
		holograms.hideHologram(u.player, "tutorial2_2");
	}

	/*
	public static void stage1(User u) {

		Player p = u.player;
		FileConfiguration config = Main.getInstance().getConfig();
		p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Stage 1/5", "/ll", 10, 100, 50);
		p.teleport(new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), config.getDouble("starting_position.x"), config.getDouble("starting_position.y"), config.getDouble("starting_position.z")));

		p.sendMessage(Utils.chat("&fThe first thing you'll be wondering, where am I?"));
		p.sendMessage(Utils.chat("&fFor this we have the command /ll"));
		p.sendMessage(Utils.chat("&fThis will return your current coordinates in chat with a link to google maps."));
		p.sendMessage(Utils.chat("&fTry it out and continue the tutorial."));

	}
	 */
	public void startStage2_1() {
		
		setHologramVisibility();
		u.player.teleport(TutorialConstants.TUTORIAL_2_START);
		u.player.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines Tutorial", "Good luck, this tutorial has 3 steps.", 10, 75, 10);	

	}
	
	public void startStage2_2() {
		
		setHologramVisibility();
		u.player.teleport(TutorialConstants.TUTORIAL_2_START);
		corner_1 = false;
		corner_2 = false;
		corner_3 = false;
		corner_4 = false;
		corner_sum = 0;		
		
	}

	public void startStage2_3() {

		setHologramVisibility();
		u.player.teleport(TutorialConstants.TUTORIAL_2_STEP3_START);
		u.player.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Outlines", "Time to create building outlines.", 10, 75, 10);
		line_1 = false;
		line_2 = false;
		line_3 = false;
		line_4 = false;
		line_sum = 0;

	}

	public void startStage9() {

		u.player.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Plot Tutorial", "Before you can build, you need to create plot", 10, 75, 10);
	}

	public String stage2Corner(double[] coords, World world) {

		Location l = new Location(world, coords[0], 143, coords[1]);

		if (l.distance(TutorialConstants.TUTORIAL_2_CORNER_1) <= 1) {
			if (corner_1) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-corner_sum) + " corners left.");
			} else {
				corner_1 = true;
				corner_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-corner_sum) + " corners left.");
			}
		} else if (l.distance(TutorialConstants.TUTORIAL_2_CORNER_2) <= 1) {
			if (corner_2) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-corner_sum) + " corners left.");
			} else {
				corner_2 = true;
				corner_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-corner_sum) + " corners left.");
			}
		} else if (l.distance(TutorialConstants.TUTORIAL_2_CORNER_3) <= 1) {
			if (corner_3) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-corner_sum) + " corners left.");
			} else {
				corner_3 = true;
				corner_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-corner_sum) + " corners left.");
			}
		} else if (l.distance(TutorialConstants.TUTORIAL_2_CORNER_4) <= 1) {
			if (corner_4) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-corner_sum) + " corners left.");
			} else {
				corner_4 = true;
				corner_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-corner_sum) + " corners left.");
			}
		}

		return (ChatColor.RED + "This is not close enough to the corner of the building.");

	}
	
	public String stage2Line(BlockVector3 p1, BlockVector3 p2) {
		
		if (TutorialConstants.TUTORIAL_2_LINE1.equals(p1, p2)) {
			if (line_1) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-line_sum) + " line(s) left.");
			} else {
				line_1 = true;
				line_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-line_sum) + " line(s) left.");
			}
		} else if (TutorialConstants.TUTORIAL_2_LINE2.equals(p1, p2)) {
			if (line_2) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-line_sum) + " line(s) left.");
			} else {
				line_2 = true;
				line_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-line_sum) + " line(s) left.");
			}
		} else if (TutorialConstants.TUTORIAL_2_LINE3.equals(p1, p2)) {
			if (line_3) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-line_sum) + " line(s) left.");
			} else {
				line_3 = true;
				line_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-line_sum) + " line(s) left.");
			}
		} else if (TutorialConstants.TUTORIAL_2_LINE4.equals(p1, p2)) {
			if (line_4) {
				return (ChatColor.RED + "You have already teleported to this corner, you have " + (4-line_sum) + " line(s) left.");
			} else {
				line_4 = true;
				line_sum += 1;
				return (ChatColor.GREEN + "Correct, you now have " + (4-line_sum) + " line(s) left.");
			}
		}

		return (ChatColor.RED + "Click on 2 corners of the building and run //line stone");
		
	}

	/*
	public static void stage3(User u) {

		Player p = u.player;
		p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Stage 3/5", "Creating a plot", 10, 100, 50);
		p.sendMessage(Utils.chat("&fNow that you know where you are you can pick out a building to build."));
		p.sendMessage(Utils.chat("&fFor this tutorial you should make a plot for 134+136 Marlborough Gardens."));
		p.sendMessage(Utils.chat("&fTo create a plot you need to mark out the corners with the selection tool."));
		p.sendMessage(Utils.chat("&fThe selection tool can be found in the gui, it's the blaze rod."));
		p.sendMessage(Utils.chat("&fIt is recommended to make your plot larger than the house and garden need."));
		p.sendMessage(Utils.chat("&fWhen you have selected the corners open the gui and click the emerald."));

	}

	public static void stage4(User u) {

		Player p = u.player;
		p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Stage 4/5", "Creating outlines", 10, 100, 50);
		p.sendMessage(Utils.chat("&fOnce you have created a plot you can start to build."));
		p.sendMessage(Utils.chat("&fBut before you can build you need to find the outlines of the building."));
		p.sendMessage(Utils.chat("&fIn google maps right click on one of the corners of the building and copy the coordinates."));
		p.sendMessage(Utils.chat("&fKeep in mind that the roof often sticks out a bit so you may want to move inward a little."));
		p.sendMessage(Utils.chat("&fWith the coordinates again use /tpll to teleport to the corner of 134+136 Marlborough Gardens."));
	}

	public static void stage5(User u) {

		Player p = u.player;
		p.sendTitle(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Stage 5/5", "Estimating the height of the building.", 10, 100, 50);
		p.sendMessage(Utils.chat("&fThe final step is to make the walls the right height."));
		p.sendMessage(Utils.chat("&fGoogle earth pro has elevations (elev) in the bottom right corner."));
		p.sendMessage(Utils.chat("&fThis can be used to get the height, or you can estimate it."));
		p.sendMessage(Utils.chat("&fIf you choose to guess the height a good reference is the door, which is usually around 2 metres."));
		p.sendMessage(Utils.chat("&fPlease type in chat your estimated height for the front wall of 134+136 Marlborough Gardens."));
	}

	public static void stage6(User u) {

		Player p = u.player;
		FileConfiguration config = Main.getInstance().getConfig();
		u.plots = new Plots();
		p.teleport(new Location(Bukkit.getWorld(config.getString("worlds.build")), config.getDouble("starting_position.x"), config.getDouble("starting_position.y"), config.getDouble("starting_position.z")));
		p.sendMessage(Utils.chat("&aYou have completed the tutorial!"));
		p.sendMessage(Utils.chat("&fHere is the building built by one of our builders."));
		p.sendMessage(Utils.chat("&fUse the gui or tpll to leave this area and go back to wherever you want to go."));
		Stage = 7;

	}
	 */

	public boolean nearCorners(double[] coords) {

		//Checks whether the player has teleported near the 4 corners of the building using /tpll
		FileConfiguration config = Main.getInstance().getConfig();

		if ((Math.abs(coords[0]-config.getDouble("tpll_points.1.x")) <= 0.5) && (Math.abs(coords[1]-config.getDouble("tpll_points.1.z")) <= 0.5)) {
			return true;
		} else if ((Math.abs(coords[0]-config.getDouble("tpll_points.2.x")) <= 0.5) && (Math.abs(coords[1]-config.getDouble("tpll_points.2.z")) <= 0.5)) {
			return true;
		} else if ((Math.abs(coords[0]-config.getDouble("tpll_points.3.x")) <= 0.5) && (Math.abs(coords[1]-config.getDouble("tpll_points.3.z")) <= 0.5)) {
			return true;
		} else if ((Math.abs(coords[0]-config.getDouble("tpll_points.4.x")) <= 0.5) && (Math.abs(coords[1]-config.getDouble("tpll_points.4.z")) <= 0.5)) {
			return true;
		} else {
			return false;
		}
	}

	public boolean getHeight(Double h) {

		//Checks whether the player has given the correct height of the building.
		FileConfiguration config = Main.getInstance().getConfig();

		if (Math.floor(h) == config.getInt("building_height")) {
			return true;
		} else {
			return false;
		}
	}

}
