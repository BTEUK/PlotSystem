package me.bteuk.plotsystem;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.commands.ClaimCommand;
import me.bteuk.plotsystem.commands.PlotSystemCommand;
import me.bteuk.plotsystem.listeners.*;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.utils.plugins.Multiverse;
import me.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;

public class PlotSystem extends JavaPlugin {

    //Logger
    public static Logger LOGGER;

    //SQL Classes.
    public GlobalSQL globalSQL;
    public PlotSQL plotSQL;

    public Timers timers;

    //Items
    public static ItemStack selectionTool;

    static PlotSystem instance;
    static FileConfiguration config;

    private ArrayList<User> users;

    public static ItemStack gui;

    //Server Name
    public static String SERVER_NAME;

    //Listeners
    public ClaimEnter claimEnter;

    @Override
    public void onEnable() {

        LOGGER = getLogger();

        //Config Setup
        PlotSystem.instance = this;
        PlotSystem.config = this.getConfig();

        saveDefaultConfig();

        if (!config.getBoolean("enabled")) {

            LOGGER.warning("The config must be configured before the plugin can be enabled!");
            LOGGER.warning("Please edit the database values in the config, give the server a unique name and then set 'enabled: true'");
            return;

        }

        //Setup MySQL
        try {

            //Global Database
            String global_database = config.getString("database.global");
            BasicDataSource global_dataSource = mysqlSetup(global_database);
            globalSQL = new GlobalSQL(global_dataSource);

            String plot_database = config.getString("database.plot");
            BasicDataSource plot_dataSource = mysqlSetup(plot_database);
            plotSQL = new PlotSQL(plot_dataSource);

        } catch (SQLException /*| IOException*/ e) {
            e.printStackTrace();
            LOGGER.severe("Failed to connect to the database, please check that you have set the config values correctly.");
            return;
        }

        //Set the server name from config.
        SERVER_NAME = config.getString("server_name");

        //If the server is in the database.
        if (globalSQL.hasRow("SELECT name FROM server_data WHERE name='" + SERVER_NAME + "';")) {

            //Add save world if it does not yet exist.
            //Save world name is in config.
            //This implies first launch with plugin.
            if (!Multiverse.hasWorld(config.getString("save_world"))) {

                //Create save world.
                if (!Multiverse.createVoidWorld(config.getString("save_world"))) {

                    LOGGER.warning("Failed to create save world!");

                }

                //Enable plugin.
                LOGGER.info("Enabling Plugin");
                enablePlugin();

            } else {

                //Save world has already been created, enable plugin.
                LOGGER.info("Enabling Plugin");
                enablePlugin();


            }
        } else {

            //If the server is not in the database the network plugin was not successful.
            LOGGER.warning("Server is not in database, check that the Network plugin is working correctly.");

        }
    }

    //Server enabling procedure when the config has been set up.
    public void enablePlugin() {

        //General Setup
        //Create list of users.
        users = new ArrayList<>();

        //Remove all plots 'under review'
        plotSQL.update("UPDATE plot_data SET status='submitted' WHERE status='reviewing'");

        //Create gui item
        gui = new ItemStack(Material.NETHER_STAR);
        ItemMeta meta2 = gui.getItemMeta();
        meta2.displayName(Utils.title("Building Menu"));
        gui.setItemMeta(meta2);

        //Setup Timers
        timers = new Timers(this, globalSQL, plotSQL);
        timers.startTimers();

        //Create bungeecord channel
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        //Create selection tool item
        selectionTool = new ItemStack(Material.BLAZE_ROD);
        ItemMeta meta = selectionTool.getItemMeta();
        meta.displayName(Utils.success("Selection Tool"));
        selectionTool.setItemMeta(meta);

        //Listeners
        new JoinServer(this, globalSQL, plotSQL);
        new QuitServer(this);
        new PlayerInteract(instance, plotSQL);
        new CloseInventory(this);

        //Deals with tracking where players are in relation to plots.
        claimEnter = new ClaimEnter(this, plotSQL, globalSQL);

        //Create instance of claim command,
        //as it is used to check whether a person is able to claim the plot they're standing in.
        ClaimCommand claimCommand = new ClaimCommand(plotSQL);

        //Commands
        getCommand("plotsystem").setExecutor(new PlotSystemCommand(globalSQL, plotSQL));
        getCommand("claim").setExecutor(claimCommand);

    }


    public void onDisable() {

        //Remove all players who are in review.
        //If users is not empty.
        if (!users.isEmpty()) {
            for (User u : users) {

                //Set tutorialStage in PlayData.
                //tutorialData.updateValues(u);

                //Update the last online time of player.
                //playerData.updateTime(u.uuid);

                //If the player is in a review, cancel it.
                if (u.review != null) {

                    PlotSQL plotSQL = PlotSystem.getInstance().plotSQL;

                    //Remove the reviewer from the plot.
                    WorldGuardFunctions.removeMember(String.valueOf(u.review.plot), u.uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + u.review.plot + ";")));

                    //Set status back to submitted.
                    plotSQL.update("UPDATE plot_data SET status='submitted' WHERE id=" + u.review.plot + ";");

                    //Close review.
                    u.review.closeReview();

                }
            }
        }

        //Disable bungeecord channel.
        this.getServer().getMessenger().unregisterOutgoingPluginChannel(this);

        LOGGER.info("Disabled PublicBuilds");
    }

    //Creates the mysql connection.
    private BasicDataSource mysqlSetup(String database) throws SQLException {

        String host = config.getString("host");
        int port = config.getInt("port");
        String username = config.getString("username");
        String password = config.getString("password");

        BasicDataSource dataSource = new BasicDataSource();

        dataSource.setUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?&useSSL=false&");
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
    public static PlotSystem getInstance() {
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

    //Add user to list.
    public void addUser(User u) {

        users.add(u);

    }

    //Get user from player.
    public void removeUser(User u) {

        users.remove(u);

    }
}