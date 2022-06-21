package me.bteuk.plotsystem.utils;

import me.bteuk.network.gui.UniqueGui;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.sql.GlobalSQL;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.reviewing.Review;

public class User {

    //Basic information about the player.
    public Player player;
    public String uuid;
    public String name;

    public SelectionTool selectionTool;

    public int inPlot = 0;
    public boolean plotOwner;
    public boolean plotMember;
    public boolean isClaimed;

    public Review review = null;

    public final GlobalSQL globalSQL;
    public final PlotSQL plotSQL;

    //Guis
    public UniqueGui claimGui;
    public UniqueGui createGui;

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
