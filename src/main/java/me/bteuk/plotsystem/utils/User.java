package me.bteuk.plotsystem.utils;

import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.utils.enums.Role;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.reviewing.Review;

public class User {

    //Basic information about the player.
    public Player player;
    public String uuid;
    public String name;

    //Important tutorial information.
    public boolean tutorial_complete;

    public int buildingTime;

    public SelectionTool selectionTool;

    public int currentPlot = 0;
    public String currentStatus = null;

    public int inPlot = 0;
    public boolean plotOwner;
    public boolean plotMember;
    public boolean isClaimed;

    public Review review = null;

    public final GlobalSQL globalSQL;
    public final PlotSQL plotSQL;

    public User(Player player, GlobalSQL globalSQL, PlotSQL plotSQL) {

        //Set sql
        this.globalSQL = globalSQL;
        this.plotSQL = plotSQL;

        //Set player, uuid and name variable.
        this.player = player;
        uuid = player.getUniqueId().toString();
        name = player.getName();

        //Set selection tool, only players with the valid roles can use it.
        selectionTool = new SelectionTool(this, plotSQL);

    }
}
