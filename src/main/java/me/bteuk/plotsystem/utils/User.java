package me.bteuk.plotsystem.utils;

import lombok.Getter;
import me.bteuk.network.sql.GlobalSQL;
import me.bteuk.network.sql.PlotSQL;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.gui.ClaimGui;
import me.bteuk.plotsystem.gui.CreatePlotGui;
import me.bteuk.plotsystem.gui.CreateZoneGui;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import me.bteuk.plotsystem.reviewing.Review;

import java.util.ArrayList;
import java.util.List;

public class User {

    //Basic information about the player.
    public final Player player;
    public final String uuid;
    public final String name;

    public final SelectionTool selectionTool;

    public int inPlot = 0;
    public int inZone = 0;

    public Review review = null;

    public final GlobalSQL globalSQL;
    public final PlotSQL plotSQL;

    // Guis
    public ClaimGui claimGui;
    public CreatePlotGui createPlotGui;
    public CreateZoneGui createZoneGui;

    // Store the location of the player on interval, this allows the server to check when to update the outlines.
    public Location lastLocation;

    // Skip outlines for these plots.
    @Getter
    private final List<String> skipOutlines = new ArrayList<>();

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

        //Set last location to current location.
        lastLocation = player.getLocation();
        //Set outlines for player.
        PlotSystem.getInstance().getOutlines().addNearbyOutlines(this);

    }
}
