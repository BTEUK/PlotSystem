package net.bteuk.plotsystem;

import java.util.ArrayList;
import java.util.logging.Logger;

import lombok.Getter;
import net.bteuk.network.Network;
import net.bteuk.network.sql.GlobalSQL;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.Utils;
import net.bteuk.plotsystem.commands.ClaimCommand;
import net.bteuk.plotsystem.commands.PlotSystemCommand;
import net.bteuk.plotsystem.exceptions.RegionManagerNotFoundException;
import net.bteuk.plotsystem.exceptions.RegionNotFoundException;
import net.bteuk.plotsystem.listeners.*;
import net.bteuk.plotsystem.listeners.ClaimEnter;
import net.bteuk.plotsystem.listeners.CloseInventory;
import net.bteuk.plotsystem.listeners.QuitServer;
import net.bteuk.plotsystem.utils.Outlines;
import net.bteuk.plotsystem.utils.plugins.Multiverse;
import net.bteuk.plotsystem.utils.plugins.WorldGuardFunctions;
import net.bteuk.plotsystem.listeners.JoinServer;
import net.bteuk.plotsystem.listeners.PlayerInteract;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.bteuk.plotsystem.utils.User;

public class PlotSystem extends JavaPlugin {

    //Logger
    public static Logger LOGGER;

    //SQL Classes.
    public GlobalSQL globalSQL;
    public PlotSQL plotSQL;

    public Timers timers;

    //Items
    public static ItemStack selectionTool;

    //Returns an instance of the plugin.
    @Getter
    static PlotSystem instance;
    static FileConfiguration config;

    //Returns the User ArrayList.
    @Getter
    private ArrayList<User> users;

    public static ItemStack gui;

    //Server Name
    public static String SERVER_NAME;

    //Listeners
    public ClaimEnter claimEnter;

    //Outline manager.
    @Getter
    private Outlines outlines;

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

        // Set databases from Network dependency.
        globalSQL = Network.getInstance().getGlobalSQL();
        plotSQL = Network.getInstance().getPlotSQL();

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
            }

            LOGGER.info("Enabling Plugin");
            enablePlugin();

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

        //Outlines, this will be accessed from other classes, so it must have a getter and setter.
        outlines = new Outlines();

        //Setup Timers
        timers = new Timers(this, globalSQL);
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
        ClaimCommand claimCommand = new ClaimCommand();

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
                    try {
                        WorldGuardFunctions.removeMember(String.valueOf(u.review.plot), u.uuid, Bukkit.getWorld(plotSQL.getString("SELECT location FROM plot_data WHERE id=" + u.review.plot + ";")));
                    } catch (RegionManagerNotFoundException | RegionNotFoundException e) {
                        e.printStackTrace();
                    }

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