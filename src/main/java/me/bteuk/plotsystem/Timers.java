package me.bteuk.plotsystem;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;

import me.bteuk.plotsystem.mysql.MessageData;
import me.bteuk.plotsystem.tutorial.TutorialConstants;
import me.bteuk.plotsystem.utils.User;

public class Timers {

	Main instance;
	ArrayList<User> users;
	
	//Blocks for outlines tutorial.
	ArrayList<Location> blocks;
	public BlockData stone;
	
	//private Boolean POINTS_ENABLED;
	
	private World tutorialWorld;
	
	private int hasMessage;
	
	//Database
	DataSource dataSource;
	MessageData messageData;

	public Timers(Main instance, FileConfiguration config, DataSource dataSource) {
		this.instance = instance;
		this.users = instance.getUsers();
		
		//Are normal points enabled
		//POINTS_ENABLED = config.getBoolean("points_enabled");
		
		//Set stone equal to stone.
		stone = Bukkit.createBlockData(Material.STONE);
		
		//Set the worlds.
		tutorialWorld = Bukkit.getWorld(config.getString("worlds.tutorial"));
		
		//Database
		this.dataSource = dataSource;
		this.messageData = instance.messageData;
		
	}

	public void startTimers() {

		//1 second timer.
		instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
			public void run() {

				for (User u : users) {

					//If the player has a correct line set in tutorial 2.3 set fake blocks every second as indicator.
					if (u.tutorial.tutorial_type == 2 && u.tutorial.tutorial_stage == 3) {

						if (u.tutorial.line_1) {
							blocks = TutorialConstants.TUTORIAL_2_LINE1.vectorBlocks();
							for (Location l : blocks) {
								u.player.sendBlockChange(l, stone);
							}
						}

						if (u.tutorial.line_2) {
							blocks = TutorialConstants.TUTORIAL_2_LINE2.vectorBlocks();
							for (Location l : blocks) {
								u.player.sendBlockChange(l, stone);
							}
						}

						if (u.tutorial.line_3) {
							blocks = TutorialConstants.TUTORIAL_2_LINE3.vectorBlocks();
							for (Location l : blocks) {
								u.player.sendBlockChange(l, stone);
							}
						}

						if (u.tutorial.line_4) {
							blocks = TutorialConstants.TUTORIAL_2_LINE4.vectorBlocks();
							for (Location l : blocks) {
								u.player.sendBlockChange(l, stone);
							}
						}		
					}

					//Increase buildingTime for each second the player is in a buildable claim and is not AFK
					/*
					if (!(u.plotOwner == null) && POINTS_ENABLED) {
						if (ess.getUser(u.player).isAfk() == false && u.plotOwner.equals(u.uuid)) {

							u.buildingTime += 1;

							if (u.buildingTime >= interval) {
								u.buildingTime -= interval;

								me.bteuk.btepoints.utils.Points.addPoints(u.uuid, 1);
							}


						}
					}
					*/

					//Set the world of the player.
					u.world = u.player.getWorld();
					if (!(u.world.getName().equals(tutorialWorld)) && u.tutorial.first_time == false) {
						u.tutorial.tutorial_stage = 0;
						u.tutorial.tutorial_type = 10;
						u.tutorial.complete = true;
					} else if (!(u.world.getName().equals(tutorialWorld)) && u.tutorial.first_time == true) {
						Bukkit.getScheduler().runTaskLater (instance, () -> u.tutorial.continueTutorial(), 60);
					}

					//Send deny or accept message if a plot has been accepted or denied that they own.
					//Will not send if they are afk.
					if (true /*custom afk check*/) {

						hasMessage = messageData.hasMessage(u.uuid);

						if (hasMessage != 0) {
							switch (messageData.getType(hasMessage)) {

							case "returned":
								u.player.sendMessage(ChatColor.RED + "Your plot " + messageData.getPlot(hasMessage) + " was denied and returned, see feedback in the plot menu.");
								break;
							case "resized":
								u.player.sendMessage(ChatColor.RED + "Your plot " + messageData.getPlot(hasMessage) + " was denied and resized, see feedback in the plot menu.");
								break;
							case "deleted":
								u.player.sendMessage(ChatColor.RED + "Your plot " + messageData.getPlot(hasMessage) + " was denied and deleted, see feedback in the plot menu.");
								break;
							case "accepted":
								u.player.sendMessage(ChatColor.GREEN + "Your plot " + messageData.getPlot(hasMessage) + " was accepted, see feedback in the plot menu.");
								break;
							case "inactive":
								u.player.sendMessage(ChatColor.RED + "Your plot " + messageData.getPlot(hasMessage) + " has been deleted due to inactivity.");
								break;
							default:
								break;							
							}

							messageData.delete(hasMessage);
						}
					}

					//Ranks.checkRankup(u);

					u.slot5 = u.player.getInventory().getItem(4);
					u.slot9 = u.player.getInventory().getItem(8);

					if (!(u.slot9 == null)) {
						if (u.slot9.equals(Main.gui)) {
						} else {
							u.player.getInventory().setItem(8, Main.gui);
						}
					} else {
						u.player.getInventory().setItem(8, Main.gui);
					}

					if (u.tutorial.complete) {
						if (!(u.slot5 == null)) {
							if (u.slot5.equals(Main.tutorialGui)) {
								u.player.getInventory().setItem(4, null);
							}
						}
					} else {
						if (!(u.slot5 == null)) {
							if (u.slot5.equals(Main.tutorialGui)) {
							} else {
								u.player.getInventory().setItem(4, Main.tutorialGui);
							}
						} else {
							u.player.getInventory().setItem(4, Main.tutorialGui);
						}
					}

					if (u.tutorial.tutorial_type == 10) {
						u.tutorial.removeHologramVisibility();
					}
				}

			}
		}, 0L, 20L);

		//1 minute timer.
		instance.getServer().getScheduler().scheduleSyncRepeatingTask(instance, new Runnable() {
			public void run() {

				instance.holograms.reload("scoreboard");
				try {
					instance.testDataSource(dataSource);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}, 0L, 1200L);
	}

}