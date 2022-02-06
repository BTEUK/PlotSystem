package me.bteuk.plotsystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.mysql.cj.jdbc.MysqlDataSource;
import me.bteuk.plotsystem.listeners.JoinServer;
import me.bteuk.plotsystem.plots.Plots;
import me.bteuk.plotsystem.serverconfig.SetupGuiEvent;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.tutorial.*;
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

import me.bteuk.plotsystem.commands.BuildingPoints;
import me.bteuk.plotsystem.commands.CreateArea;
import me.bteuk.plotsystem.commands.OpenGui;
import me.bteuk.plotsystem.commands.Spawn;
import me.bteuk.plotsystem.gui.ConfirmCancel;
import me.bteuk.plotsystem.gui.LocationGUI;
import me.bteuk.plotsystem.gui.MainGui;
import me.bteuk.plotsystem.gui.PlotGui;
import me.bteuk.plotsystem.gui.PlotInfo;
import me.bteuk.plotsystem.gui.SwitchServerGUI;
import me.bteuk.plotsystem.reviewing.AcceptGui;
import me.bteuk.plotsystem.reviewing.DenyGui;
import me.bteuk.plotsystem.reviewing.FeedbackGui;
import me.bteuk.plotsystem.reviewing.ReviewGui;
import me.bteuk.plotsystem.serverconfig.JoinEvent;
import me.bteuk.plotsystem.serverconfig.SetupGui;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Holograms;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.WorldGuardFunctions;

public class Main extends JavaPlugin {

    //MySQL
    private String host, username, password;
    private int port;

    //Global Database
    private String global_database;
    private GlobalSQL globalSQL;
    private DataSource global_dataSource;

    //Plot Database
    private String plot_database;
    private PlotSQL plotSQL;
    private DataSource plot_dataSource;

    //Navigation Database
    private String navigation_database;
    private NavigationSQL navigationSQL;
    private DataSource navigation_dataSource;

    public Timers timers;

    //Plots
    public Plots plots;
    public static boolean PLOTS_ONLY;

    //Tutorial
    public Tutorial tutorial;
    public static boolean TUTORIAL_ONLY;


    //Other
    public static Permission perms = null;

    static Main instance;
    static FileConfiguration config;

    private ArrayList<User> users;

    public static ItemStack gui;

    int interval;

    //Locations
    public static Location spawn;
    public static Location cranham;
    public static Location monkspath;

    //Holograms
    Holograms holograms;

    //Server Name
    public static String SERVER_NAME;

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

            Bukkit.getLogger().warning(Utils.chat("&cThe config must be configured before the plugin can be enabled!"));
            Bukkit.getLogger().warning(Utils.chat("&cPlease edit the database values in the config, give the server a unique name and then set 'enabled: true'"));
            return;

        }

        //Setup MySQL
        try {

            global_database = config.getString("database.global");
            global_dataSource = mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);

            plot_database = config.getString("database.plot");
            plot_dataSource = mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);

            navigation_database = config.getString("database.navigation");
            navigation_dataSource = mysqlSetup(navigation_database);
            navigationSQL = new NavigationSQL(navigation_dataSource);

        } catch (SQLException /*| IOException*/ e) {
            e.printStackTrace();
            Bukkit.getLogger().severe(Utils.chat("&cFailed to connect to the database, please check that you have set the config values correctly."));
            return;
        }

        SERVER_NAME = config.getString("server_name");

        if (!plotSQL.serverSetup(SERVER_NAME)) {

            Bukkit.getLogger().warning(Utils.chat("&cThe server has not yet been configured, please join to configure the server!"));
            configureServer();

        } else {

            enableServer();

        }
    }

    public void configureServer() {

        new JoinEvent(this);
        new SetupGuiEvent(this, navigationSQL, plotSQL);
        SetupGui.initialize();

    }

    public void enableServer() {

        //General Setup
        //Create list of users.
        users = new ArrayList<>();

        //Create gui item
        gui = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta2 = gui.getItemMeta();
        meta2.setLocalizedName(ChatColor.AQUA + "" + ChatColor.BOLD + "Building Menu");
        gui.setItemMeta(meta2);

        //Set the global static variable indicating whether the server is limited to a single task.
        PLOTS_ONLY = plotSQL.plotsOnly();
        TUTORIAL_ONLY = plotSQL.tutorialOnly();

        //plotData.clearReview();

        //Global Join listener
        new JoinServer(instance, plotSQL);

        //Setup Timers
        timers = new Timers(this, config, plot_dataSource);
        timers.startTimers();

        //Create bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        if (!TUTORIAL_ONLY) {

            //Setup plots
            plots = new Plots(this, plotSQL, globalSQL);
            plots.setup();

        }

        if (!PLOTS_ONLY) {

            //Setup tutorial
            tutorial = new Tutorial(this);
            tutorial.setup();

        }

        //Holograms
        //holograms = new Holograms(hologramData, hologramText, playerData);
        //holograms.create();

        //Commands
        getCommand("gui").setExecutor(new OpenGui());
        getCommand("createarea").setExecutor(new CreateArea());
        getCommand("buildingpoints").setExecutor(new BuildingPoints());
        getCommand("spawn").setExecutor(new Spawn());
        getCommand("tutorial").setExecutor(new TutorialCommand());
        getCommand("tutorialStage").setExecutor(new TutorialStage());

        //getCommand("customholo").setExecutor(new CustomHolo(hologramData, hologramText, holograms));

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

    }


    public void onDisable() {

        //Remove all players who are in review.
        for (User u : users) {

            //Set tutorialStage in PlayData.
            //tutorialData.updateValues(u);

            //Update the last online time of player.
            //playerData.updateTime(u.uuid);

            //If the player is in a review, cancel it.
            if (u.review != null) {

                WorldGuardFunctions.removeMember(u.review.plot, u.uuid);
                //plotData.setStatus(u.review.plot, "submitted");
                u.review.editBook.unregister();
                u.player.getInventory().setItem(4, u.review.previousItem);
                u.review = null;

            }
        }

        //Disable bungeecord channel.
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        Bukkit.getLogger().info("Disabled PublicBuilds");
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

    public void testDataSource(DataSource dataSource) throws SQLException {
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