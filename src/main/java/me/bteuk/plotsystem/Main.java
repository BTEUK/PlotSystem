package me.bteuk.plotsystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import me.bteuk.plotsystem.commands.ClaimCommand;
import me.bteuk.plotsystem.commands.PlotSystem;
import me.bteuk.plotsystem.gui.*;
import me.bteuk.plotsystem.listeners.InventoryClicked;
import me.bteuk.plotsystem.listeners.ItemSpawn;
import me.bteuk.plotsystem.listeners.JoinServer;
import me.bteuk.plotsystem.listeners.PlayerInteract;
import me.bteuk.plotsystem.listeners.ClaimEnter;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.NavigationSQL;
import me.bteuk.plotsystem.voidgen.VoidChunkGen;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.bteuk.plotsystem.commands.BuildingPoints;
import me.bteuk.plotsystem.commands.OpenGui;
import me.bteuk.plotsystem.reviewing.AcceptGui;
import me.bteuk.plotsystem.reviewing.DenyGui;
import me.bteuk.plotsystem.reviewing.FeedbackGui;
import me.bteuk.plotsystem.reviewing.ReviewGui;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.Holograms;
import me.bteuk.plotsystem.utils.User;
import me.bteuk.plotsystem.utils.Utils;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;

public class Main extends JavaPlugin {

    //MySQL
    private String host, username, password;
    private int port;

    //Global Database
    private String global_database;
    private GlobalSQL globalSQL;
    private BasicDataSource global_dataSource;

    //Plot Database
    private String plot_database;
    public PlotSQL plotSQL;
    private BasicDataSource plot_dataSource;

    //Navigation Database
    private String navigation_database;
    private NavigationSQL navigationSQL;
    private BasicDataSource navigation_dataSource;

    public Timers timers;

    //Items
    public static ItemStack selectionTool;

    static Main instance;
    static FileConfiguration config;

    private ArrayList<User> users;

    public static ItemStack gui;

    int interval;

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

            navigation_database = config.getString("database.navigation");
            navigation_dataSource = mysqlSetup(navigation_database);
            navigationSQL = new NavigationSQL(navigation_dataSource);

            plot_database = config.getString("database.plot");
            plot_dataSource = mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource, navigationSQL);

        } catch (SQLException /*| IOException*/ e) {
            e.printStackTrace();
            Bukkit.getLogger().severe(Utils.chat("&cFailed to connect to the database, please check that you have set the config values correctly."));
            return;
        }

        //Set the server name from config.
        SERVER_NAME = config.getString("server_name");

        if (!navigationSQL.hasRow("SELECT name FROM server_data WHERE name=" + SERVER_NAME + ";")) {

            //Add server to database and enable server.
            if (navigationSQL.insert(
                    "INSERT INTO server_data(name,type) VALUES(" + SERVER_NAME + ",'plot');"
            )) {

                Bukkit.getLogger().info(Utils.chat("&aServer added to database, enabling server!"));
                enableServer();

                //If it fails close the plugin.
            } else {
                Bukkit.getLogger().severe(Utils.chat("&cFailed to add server to database, shutting down PlotSystem!"));
            }
        }
    }

    //Server enabling procedure when the config has been set up.
    public void enableServer() {

        //General Setup
        //Create list of users.
        users = new ArrayList<>();

        //Create gui item
        gui = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta2 = gui.getItemMeta();
        meta2.setLocalizedName(ChatColor.AQUA + "" + ChatColor.BOLD + "Building Menu");
        gui.setItemMeta(meta2);

        //plotData.clearReview();

        //Global Join listener
        new JoinServer(instance, globalSQL, plotSQL);

        //Setup Timers
        timers = new Timers(this, globalSQL);
        timers.startTimers();

        //Create bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        //Create selection tool item
        selectionTool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = selectionTool.getItemMeta();
        meta.setLocalizedName(Utils.chat("&aSelection Tool"));
        selectionTool.setItemMeta(meta);

        //Listeners
        //new QuitServer(this, tutorialData, playerData, plotData);
        new InventoryClicked(instance);
        new PlayerInteract(instance, plotSQL);
        new ItemSpawn(instance);

        //Deals with tracking where players are in relation to plots.
        new ClaimEnter(instance, plotSQL, globalSQL);

        //Holograms
        //holograms = new Holograms(hologramData, hologramText, playerData);
        //holograms.create();

        //Create instance of claim command,
        //as it is used to check whether a person is able to claim the plot they're standing in.
        ClaimCommand claimCommand = new ClaimCommand(plotSQL);

        //Commands
        getCommand("plotsystem").setExecutor(new PlotSystem(plotSQL, navigationSQL));
        getCommand("claim").setExecutor(claimCommand);


        getCommand("gui").setExecutor(new OpenGui());
        getCommand("buildingpoints").setExecutor(new BuildingPoints());

        //getCommand("customholo").setExecutor(new CustomHolo(hologramData, hologramText, holograms));

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
        FeedbackGui.initialize();

        ClaimGui.initialize();

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

                WorldGuardFunctions.removeMember(u.review.plot, u.uuid, Bukkit.getWorld(plotSQL.getString("SELECT world FROM location_data WHERE name=" +
                        plotSQL.getString(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + u.review.plot + ";")) + ";")));
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
    private BasicDataSource mysqlSetup(String database) throws SQLException {

        host = config.getString("host");
        port = config.getInt("port");
        username = config.getString("username");
        password = config.getString("password");

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?&useSSL=false&");
        //dataSource.setServerName(host);
        //dataSource.setPortNumber(port);
        //dataSource.setDatabaseName(database + "?&useSSL=false&");
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        testDataSource(dataSource);
        return dataSource;

    }

    public void testDataSource(BasicDataSource dataSource) throws SQLException {
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