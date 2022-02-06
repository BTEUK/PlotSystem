package me.bteuk.plotsystem.utils;

import me.bteuk.plotsystem.plots.PlotFunctions;
import me.bteuk.plotsystem.sql.GlobalSQL;
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

    //Player role.
    public Role role;


    public int buildingTime;

    public PlotFunctions plotFunctions;
    public PlotCreateInfo plotCreateInfo;

    public int currentPlot = 0;
    public String currentStatus = null;
    public int attempt;

    public int inPlot = 0;
    public boolean plotOwner;
    public boolean plotMember;

    public World world;

    public ItemStack slot5;
    public ItemStack slot9;

    public TutorialInfo tutorial;

    public String previousGui;

    TutorialData tutorialData;
    PlayerData playerData;

    public Review review = null;

    private final GlobalSQL globalSQL;

    public User(Player player, GlobalSQL globalSQL) {

        //Set sql
        this.globalSQL = globalSQL;

        //Set player, uuid and name variable.
        this.player = player;
        uuid = player.getUniqueId().toString();
        name = player.getName();

        //Update player info.
        updatePlayerData();

        //Continue the tutorial from where they last were.
        if (!(tutorialData.tutorialComplete(uuid))) {
            tutorial = new TutorialInfo(this);
            Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> tutorial.continueTutorial(), 20);
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

        if (globalSQL.playerExists(uuid)) {

            //If true then update their last online time and username.
            globalSQL.updateTime(uuid);
            globalSQL.updatePlayerName(uuid, player.getName());
            globalSQL.updateRole(uuid, role);

        } else {

            globalSQL.createPlayerInstance(player.getUniqueId().toString(),
                    player.getName(), role);

        }
    }
}
