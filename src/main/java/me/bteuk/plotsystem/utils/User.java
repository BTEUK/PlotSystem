package me.bteuk.plotsystem.utils;

import me.bteuk.plotsystem.plots.SelectionTool;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.utils.enums.Role;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.bteuk.plotsystem.mysql.PlayerData;
import me.bteuk.plotsystem.reviewing.Review;

public class User {

    //Basic information about the player.
    public Player player;
    public String uuid;
    public String name;

    //Important tutorial information.
    public boolean tutorial_complete;

    //Player role.
    public Role role;


    public int buildingTime;

    public SelectionTool selectionTool;

    public int currentPlot = 0;
    public String currentStatus = null;

    public int inPlot = 0;
    public boolean plotOwner;
    public boolean plotMember;

    public World world;

    public String previousGui;

    public Review review = null;

    private final GlobalSQL globalSQL;
    public final PlotSQL plotSQL;

    public User(Player player, GlobalSQL globalSQL, PlotSQL plotSQL) {

        //Set sql
        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

        //Set player, uuid and name variable.
        this.player = player;
        uuid = player.getUniqueId().toString();
        name = player.getName();

        //Update player info.
        updatePlayerData();

        //Continue the tutorial from where they last were.
        //if tutorial is incomplete

        //If the player is architect or above create plot creation functions.
        if (role == Role.ARCHITECT || role == Role.REVIEWER) {
            selectionTool = new SelectionTool(this, plotSQL);
        }

        //Set current world
        world = player.getWorld();

    }

    //Update playerdata or create a new instance if it's their first time joining the server.
    public void updatePlayerData() {

        //Set the role of the player.
        if (player.hasPermission("group.reviewer")) {
            role = Role.REVIEWER;
        } else if (player.hasPermission("group.architect")) {
            role = Role.ARCHITECT;
        } else if (player.hasPermission("group.builder")) {
            role = Role.BUILDER;
        } else if (player.hasPermission("group.jrbuilder")) {
            role = Role.JRBUILDER;
        } else if (player.hasPermission("group.apprentice")) {
            role = Role.APPRENTICE;
        } else if (player.hasPermission("group.applicant")) {
            role = Role.APPLICANT;
        } else {
            role = Role.GUEST;
        }

        if (globalSQL.hasRow("SELECT uuid FROM player_data WHERE uuid = " + uuid + ";")) {

            //If true then update their last online time and username.
            globalSQL.update("UPDATE player_data SET last_online = " + Time.currentTime() + " WHERE uuid = " + uuid + ";");
            globalSQL.update("UPDATE player_data SET name = " + player.getName() + " WHERE uuid = " + uuid + ";");
            globalSQL.update("UPDATE player_data SET role = " + role + " WHERE uuid = " + uuid + ";");

        } else {

            globalSQL.update("INSERT INTO player_data(uuid, name, role, last_online, last_submit) " +
                    "VALUES(" + player.getUniqueId().toString() + ", " + player.getName() + ", " + role +", " + Time.currentTime() + ", " + 0 + ");");

        }
    }
}
