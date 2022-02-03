package me.bteuk.plotsystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import me.bteuk.plotsystem.voidgen.VoidChunkGen;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;

import me.bteuk.plotsystem.commands.BuildingPoints;
import me.bteuk.plotsystem.commands.CreateArea;
import me.bteuk.plotsystem.commands.CustomHolo;
import me.bteuk.plotsystem.commands.OpenGui;
import me.bteuk.plotsystem.commands.Spawn;
import me.bteuk.plotsystem.gui.ConfirmCancel;
import me.bteuk.plotsystem.gui.LocationGUI;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.gui.PlotGui;
import me.bteuk.plotsystem.gui.PlotInfo;
import me.bteuk.plotsystem.gui.SwitchServerGUI;
import me.bteuk.plotsystem.listeners.ClaimEnter;
import me.bteuk.plotsystem.listeners.InventoryClicked;
import me.bteuk.plotsystem.listeners.ItemSpawn;
import me.bteuk.plotsystem.listeners.JoinServer;
import me.bteuk.plotsystem.listeners.PlayerInteract;
import me.bteuk.plotsystem.listeners.QuitServer;
import me.bteuk.plotsystem.mysql.AcceptData;
import me.bteuk.plotsystem.mysql.AreaData;
import me.bteuk.plotsystem.mysql.BookData;
import me.bteuk.plotsystem.mysql.DenyData;
import me.bteuk.plotsystem.mysql.HologramData;
import me.bteuk.plotsystem.mysql.HologramText;
import me.bteuk.plotsystem.mysql.MessageData;
import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.mysql.PlotData;
import me.bteuk.plotsystem.mysql.PointsData;
import me.bteuk.plotsystem.mysql.TutorialData;
import me.bteuk.plotsystem.reviewing.AcceptGui;
import me.bteuk.plotsystem.reviewing.DenyGui;
import me.bteuk.plotsystem.reviewing.FeedbackGui;
import me.bteuk.plotsystem.reviewing.ReviewGui;
import me.bteuk.plotsystem.serverconfig.JoinEvent;
import me.bteuk.plotsystem.serverconfig.SetupGui;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.tutorial.CommandListener;
import me.bteuk.plotsystem.tutorial.MoveEvent;
import me.bteuk.plotsystem.tutorial.Tutorial;
import me.bteuk.plotsystem.tutorial.TutorialCommand;
import me.bteuk.plotsystem.tutorial.TutorialConstants;
import me.bteuk.plotsystem.tutorial.TutorialGui;
import me.bteuk.plotsystem.tutorial.TutorialSelectionGui;
import me.bteuk.plotsystem.tutorial.TutorialStage;
import me.bteuk.plotsystem.tutorial.TutorialTabCompleter;
import me.bteuk.plotsystem.tutorial.TutorialVideoGui;
import me.bteuk.plotsystem.utils.Holograms;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.WorldGuardFunctions;

public class Main extends JavaPlugin {

	//MySQL
	private String host, username, password;
	private int port;
	
	//Plot Database
	private String plot_database;
	private PlotSQL plotSQL; 
	private DataSource plot_dataSource;

	public PlayerData playerData;
	public PlotData plotData;
	public TutorialData tutorialData;
	public AreaData areaData;
	public DenyData denyData;
	public AcceptData acceptData;
	public BookData bookData;
	public MessageData messageData;
	public PointsData pointsData;
	public HologramData hologramData;
	public HologramText hologramText;
	
	public Timers timers;


	//Other
	public static Permission perms = null;

	static Main instance;
	static FileConfiguration config;

	ArrayList<User> users;

	Tutorial tutorial;
	HashMap<Integer, String> pl;
	List<BlockVector2> pt;
	Location lo;
	ArrayList<Integer> pls;

	public static StateFlag CREATE_PLOT_GUEST;
	public static StateFlag CREATE_PLOT_APPRENTICE;
	public static StateFlag CREATE_PLOT_JRBUILDER;

	public static ItemStack selectionTool;
	public static ItemStack gui;
	public static ItemStack tutorialGui;

	int interval;

	//Locations
	public static Location spawn;
	public static Location cranham;
	public static Location monkspath;

	//Holograms
	Holograms holograms;
	
	//Server Name
	public static String SERVER_TYPE;

	//Set the default world generation to be void.
	@Override
	public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
		return new VoidChunkGen(this);
	}

	@Override
	public void onEnable() {

		//Config Setup
		Main.instance = this;
		Main.config = this.getConfig();

		saveDefaultConfig();
		
		if (!config.getBoolean("enabled")) {
			
			Bukkit.getConsoleSender().sendMessage(Utils.chat("&cThe config must be configured before the plugin can be enabled!"));
			Bukkit.getConsoleSender().sendMessage(Utils.chat("&cPlease edit the database values in the config and then set 'enabled: true'"));
			return;
			
		}
		
		SERVER_TYPE = "Plot";

		//Setup MySQL		
		try {
			
			plot_database = config.getString("database.plot");
			plot_dataSource = mysqlSetup(plot_database);		
			plotSQL = new PlotSQL(plot_dataSource);

			/*
			playerData = new PlayerData(dataSource);
			plotData = new PlotData(dataSource);
			tutorialData = new TutorialData(dataSource);
			areaData = new AreaData(dataSource);
			denyData = new DenyData(dataSource);
			acceptData = new AcceptData(dataSource);
			bookData = new BookData(dataSource);
			messageData = new MessageData(dataSource);
			pointsData = new PointsData(dataSource);
			hologramData = new HologramData(dataSource);
			hologramText = new HologramText(dataSource);
			*/

		} catch (SQLException /*| IOException*/ e) {
			e.printStackTrace();
			Bukkit.getConsoleSender().sendMessage(Utils.chat("&cFailed to connect to the database, please check that you have set the config values correctly."));
			return;
		}
		
		if (!plotSQL.serverSetup(config.getString("server_name"))) {
			
			Bukkit.getConsoleSender().sendMessage(Utils.chat("&cThe server has not yet been configured, join the server and run the command /configure"));
			configureServer();
			
		} else {
			
			enableServer();
			
		}
	}
	
	public void configureServer() {
		
		new JoinEvent(this);
		SetupGui.initialize();
		
	}
	
	public void enableServer() {
		
		plotData.clearReview();

		//Bungeecord
		this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

		//Create list of users.
		users = new ArrayList<User>();

		//Create selection tool item				
		selectionTool = new ItemStack(Material.BLAZE_ROD);
		ItemMeta meta = selectionTool.getItemMeta();
		meta.setDisplayName(Utils.chat("&aSelection Tool"));
		selectionTool.setItemMeta(meta);

		//Create gui item				
		gui = new ItemStack(Material.NETHER_STAR);
		ItemMeta meta2 = gui.getItemMeta();
		meta2.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Building Menu");
		gui.setItemMeta(meta2);

		//Create tutorial type skip item				
		tutorialGui = new ItemStack(Material.LECTERN);
		ItemMeta meta3 = gui.getItemMeta();
		meta3.setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Tutorial Menu");
		tutorialGui.setItemMeta(meta3);

		//Holograms
		holograms = new Holograms(hologramData, hologramText, playerData);
		holograms.create();

		//Listeners
		new JoinServer(this);
		new QuitServer(this, tutorialData, playerData, plotData);
		new InventoryClicked(this);
		new ClaimEnter(this, plotData, playerData);
		new PlayerInteract(this, plotSQL);

		new ItemSpawn(this);

		//Tutorial listeners
		new CommandListener(this);
		new MoveEvent(this);

		//Commands
		getCommand("gui").setExecutor(new OpenGui());
		getCommand("createarea").setExecutor(new CreateArea());
		getCommand("buildingpoints").setExecutor(new BuildingPoints());
		getCommand("spawn").setExecutor(new Spawn());
		getCommand("tutorial").setExecutor(new TutorialCommand());
		getCommand("tutorialStage").setExecutor(new TutorialStage());

		getCommand("customholo").setExecutor(new CustomHolo(hologramData, hologramText, holograms));

		//Tab Completer
		getCommand("tutorial").setTabCompleter(new TutorialTabCompleter());

		//GUIs
		MainGui.initialize();
		ReviewGui.initialize();
		AcceptGui.initialize();
		DenyGui.initialize();
		PlotGui.initialize();
		PlotInfo.initialize();
		LocationGUI.initialize();
		ConfirmCancel.initialize();
		SwitchServerGUI.initialize();
		TutorialGui.initialize();
		TutorialVideoGui.initialize();
		TutorialSelectionGui.initialize();
		FeedbackGui.initialize();

		//Setup Tutorial Constants
		new TutorialConstants(config);
		
		//Setup Timers
		timers = new Timers(this, config, plot_dataSource);
		timers.startTimers();
		
	}


	public void onDisable() {

		//Remove all players who are in review.
		for (User u: users) {

			//Set tutorialStage in PlayData.
			tutorialData.updateValues(u);

			//Update the last online time of player.
			playerData.updateTime(u.uuid);

			//If the player is in a review, cancel it.
			if (u.review != null) {

				WorldGuardFunctions.removeMember(u.review.plot, u.uuid);
				plotData.setStatus(u.review.plot, "submitted");
				u.review.editBook.unregister();
				u.player.getInventory().setItem(4, u.review.previousItem);
				u.review = null;

			}
		}

		Bukkit.getConsoleSender().sendMessage("Disabled PublicBuilds");
	}

	//Creates the mysql connection.
	private DataSource mysqlSetup(String database) throws SQLException {

		host = config.getString("host");
		port = config.getInt("port");
		database = config.getString("database");
		username = config.getString("username");
		password = config.getString("password");

		MysqlDataSource dataSource = new MysqlConnectionPoolDataSource();

		dataSource.setServerName(host);
		dataSource.setPortNumber(port);
		dataSource.setDatabaseName(database + "?&useSSL=false&");
		dataSource.setUser(username);
		dataSource.setPassword(password);

		testDataSource(dataSource);
		return dataSource;

	}

	public void testDataSource(DataSource dataSource) throws SQLException{
		try (Connection connection = dataSource.getConnection()) {
			if (!connection.isValid(1000)) {
				throw new SQLException("Could not establish database connection.");
			}
		}
	}

	//Returns an instance of the plugin.
	public static Main getInstance() {
		return instance;
	}

	//Returns the User ArrayList.
	public ArrayList<User> getUsers() {
		return users;
	}

	//Returns the specific user based on Player instance.
	public User getUser(Player p) {

		for (User u : users) {

			if (u.player.equals(p)) {
				return u;
			}

		}

		return null;
	}
	
	public Holograms getHolograms() {
		return holograms;
	}
}