package me.bteuk.plotsystem.tutorial;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;

import com.sk89q.worldedit.math.BlockVector3;

import me.bteuk.plotsystem.utils.Vector;

public class TutorialConstants {

	//Tutorial
	public static Location TUTORIAL_1_TELEPORT, TUTORIAL_1_TELEPORT2, TUTORIAL_1_START, TUTORIAL_1_YES, TUTORIAL_1_NO;	
	public static Location TUTORIAL_2_START, TUTORIAL_2_CORNER_1, TUTORIAL_2_CORNER_2, TUTORIAL_2_CORNER_3, TUTORIAL_2_CORNER_4, TUTORIAL_2_STEP3_START;
	public static Location TUTORIAL_3_START, TUTORIAL_3_CONTINUE, TUTORIAL_3_WORLDEDIT, TUTORIAL_3_GEP, TUTORIAL_3_ROOFS, TUTORIAL_3_DETAILS, TUTORIAL_3_TEXTURE;
	public static Location TUTORIAL_4_START;
	public static Location TUTORIAL_5_START;
	public static Location TUTORIAL_6_START;
	public static Location TUTORIAL_7_START;
	public static Location TUTORIAL_8_START;
	public static Location TUTORIAL_9_START;	
	
	public static Vector TUTORIAL_2_LINE1, TUTORIAL_2_LINE2, TUTORIAL_2_LINE3, TUTORIAL_2_LINE4;
	

	public TutorialConstants(FileConfiguration config) {

		//Tutorial Locations
		TUTORIAL_1_TELEPORT = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				0.5, 64, -19.5, 0, 0);
		TUTORIAL_1_TELEPORT2 = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")),
				0.5, 64, 0.5, 0, 0);
		TUTORIAL_1_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_1.start.x"),
				config.getDouble("tutorial_1.start.y"),
				config.getDouble("tutorial_1.start.z"));
		TUTORIAL_1_YES = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_1.good.x"),
				config.getDouble("tutorial_1.good.y"),
				config.getDouble("tutorial_1.good.z"));
		TUTORIAL_1_NO = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_1.bad.x"),
				config.getDouble("tutorial_1.bad.y"),
				config.getDouble("tutorial_1.bad.z"));

		TUTORIAL_2_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_2.start.x"),
				config.getDouble("tutorial_2.start.y"),
				config.getDouble("tutorial_2.start.z"),
				(float) config.getDouble("tutorial_2.start.yaw"),
				(float) config.getDouble("tutorial_2.start.pitch"));
		TUTORIAL_2_CORNER_1 = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_2.corner_1.x"),
				config.getDouble("tutorial_2.corner_1.y"),
				config.getDouble("tutorial_2.corner_1.z"));
		TUTORIAL_2_CORNER_2 = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_2.corner_2.x"),
				config.getDouble("tutorial_2.corner_2.y"),
				config.getDouble("tutorial_2.corner_2.z"));
		TUTORIAL_2_CORNER_3 = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_2.corner_3.x"),
				config.getDouble("tutorial_2.corner_3.y"),
				config.getDouble("tutorial_2.corner_3.z"));
		TUTORIAL_2_CORNER_4 = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_2.corner_4.x"),
				config.getDouble("tutorial_2.corner_4.y"),
				config.getDouble("tutorial_2.corner_4.z"));
		TUTORIAL_2_STEP3_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")),
				1014.5, 143, 943.5, -90, 0);
		
		//Values are being hardcoded for the time being, the whole coordinate storing system will be moved to a database in the next major release.
		TUTORIAL_2_LINE1 = new Vector(BlockVector3.at(1019, 143, 931), BlockVector3.at(1003, 143, 911));
		TUTORIAL_2_LINE2 = new Vector(BlockVector3.at(1003, 143, 911), BlockVector3.at(984, 143, 926));
		TUTORIAL_2_LINE3 = new Vector(BlockVector3.at(984, 143, 926), BlockVector3.at(1000, 143, 946));
		TUTORIAL_2_LINE4 = new Vector(BlockVector3.at(1000, 143, 946), BlockVector3.at(1019, 143, 931));
		
		TUTORIAL_3_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.start.x"),
				config.getDouble("tutorial_3.start.y"),
				config.getDouble("tutorial_3.start.z"),
				(float) config.getDouble("tutorial_3.start.yaw"),
				(float) config.getDouble("tutorial_3.start.pitch"));
		TUTORIAL_3_CONTINUE = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.continue.x"),
				config.getDouble("tutorial_3.continue.y"),
				config.getDouble("tutorial_3.continue.z"));
		TUTORIAL_3_WORLDEDIT = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.worldedit.x"),
				config.getDouble("tutorial_3.worldedit.y"),
				config.getDouble("tutorial_3.worldedit.z"));
		TUTORIAL_3_GEP = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.gep.x"),
				config.getDouble("tutorial_3.gep.y"),
				config.getDouble("tutorial_3.gep.z"));
		TUTORIAL_3_ROOFS = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.roofs.x"),
				config.getDouble("tutorial_3.roofs.y"),
				config.getDouble("tutorial_3.roofs.z"));
		TUTORIAL_3_DETAILS = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.details.x"),
				config.getDouble("tutorial_3.details.y"),
				config.getDouble("tutorial_3.details.z"));
		TUTORIAL_3_TEXTURE = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_3.texture.x"),
				config.getDouble("tutorial_3.texture.y"),
				config.getDouble("tutorial_3.texture.z"));

		/*
			TUTORIAL_4_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
					config.getDouble("tutorial_4.start.x"),
					config.getDouble("tutorial_4.start.y"),
					config.getDouble("tutorial_4.start.z"),
					(float) config.getDouble("tutorial_4.start.yaw"),
					(float) config.getDouble("tutorial_4.start.pitch"));

			TUTORIAL_5_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
					config.getDouble("tutorial_5.start.x"),
					config.getDouble("tutorial_5.start.y"),
					config.getDouble("tutorial_5.start.z"),
					(float) config.getDouble("tutorial_5.start.yaw"),
					(float) config.getDouble("tutorial_5.start.pitch"));

			TUTORIAL_6_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
					config.getDouble("tutorial_6.start.x"),
					config.getDouble("tutorial_6.start.y"),
					config.getDouble("tutorial_6.start.z"),
					(float) config.getDouble("tutorial_6.start.yaw"),
					(float) config.getDouble("tutorial_6.start.pitch"));

			TUTORIAL_7_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
					config.getDouble("tutorial_7.start.x"),
					config.getDouble("tutorial_7.start.y"),
					config.getDouble("tutorial_7.start.z"),
					(float) config.getDouble("tutorial_7.start.yaw"),
					(float) config.getDouble("tutorial_7.start.pitch"));

			TUTORIAL_8_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
					config.getDouble("tutorial_8.start.x"),
					config.getDouble("tutorial_8.start.y"),
					config.getDouble("tutorial_8.start.z"),
					(float) config.getDouble("tutorial_8.start.yaw"),
					(float) config.getDouble("tutorial_8.start.pitch"));
		 */
		TUTORIAL_9_START = new Location(Bukkit.getWorld(config.getString("worlds.tutorial")), 
				config.getDouble("tutorial_9.start.x"),
				config.getDouble("tutorial_9.start.y"),
				config.getDouble("tutorial_9.start.z"),
				(float) config.getDouble("tutorial_9.start.yaw"),
				(float) config.getDouble("tutorial_9.start.pitch"));

	}

}
