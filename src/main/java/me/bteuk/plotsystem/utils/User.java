package me.bteuk.plotsystem.utils;

import me.bteuk.plotsystem.plots.PlotFunctions;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.TutorialData;
import me.bteuk.plotsystem.reviewing.Review;
import me.bteuk.plotsystem.tutorial.TutorialInfo;

public class User {

	//Basic information about the player.
	public Player player;
	public String uuid;
	public String name;

	//Important tutorial information.
	public boolean tutorial_complete;




	public int buildingTime;
	
	public PlotFunctions plotFunctions;
	public int currentPlot = 0;
	public String currentStatus = null;
	public int attempt;
	
	public int inPlot = 0;
	public boolean plotOwner;
	public boolean plotMember;
	
	public String role;
	
	public World world;
	
	public ItemStack slot5;
	public ItemStack slot9;
	
	public TutorialInfo tutorial;
	
	public String previousGui;
	
	TutorialData tutorialData;
	PlayerData playerData;
	
	public Review review = null;
	
	public User(Player player) {
		
		//Set player, uuid and name variable.
		this.player = player;
		uuid = player.getUniqueId().toString();
		name = player.getName();
		
		tutorialData = Main.getInstance().tutorialData;
		playerData = Main.getInstance().playerData;
		
		//Update player data.
		updatePlayerData();
		
		//Get building time
		/*if (Main.POINTS_ENABLED) {
			buildingTime = me.bteuk.btepoints.utils.PlayerData.getBuildTime(uuid);
		}
		*/
			
		//Continue the tutorial from where they last were.
		if (!(tutorialData.tutorialComplete(uuid))) {
			tutorial = new TutorialInfo(this);
			Bukkit.getScheduler().runTaskLater (Main.getInstance(), () -> tutorial.continueTutorial(), 20);
		} else {
			tutorial = new TutorialInfo(this, true);
			Ranks.applicant(this);
		}
		
		//Create plot functions class.
		//This handles the selection and creation of new plots.
		plotFunctions = new PlotFunctions(this);
		
		//Set current world
		world = player.getWorld();

	}
	
	//Update playerdata or create a new instance if it's their first time joining the server.
	public void updatePlayerData() {
		

		if (player.hasPermission("group.builder")) {
			role = "builder";
		} else if (player.hasPermission("group.jrbuilder")) {
			role = "jrbuilder";
		} else if (player.hasPermission("group.apprentice")) {
			role = "apprentice";
		} else if (player.hasPermission("group.applicant")) {
			role = "applicant";
		} else {
			role = "guest";
		}
		
		if (playerData.playerExists(uuid)) {
			
			//If true then update their last online time and username.
			playerData.updateTime(uuid);
			playerData.updatePlayerName(uuid, player.getName());
			playerData.updateRole(uuid, role);
		} else {
			
			playerData.createPlayerInstance(player.getUniqueId().toString(), player.getName(), role);
			
		}
		
	}
}
