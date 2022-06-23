package me.bteuk.plotsystem.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.PlotOutline;
import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ClaimEnter implements Listener {

    PlotSQL plotSQL;
    GlobalSQL globalSQL;

    PlotOutline plotOutline;
    int plotID;
    int difficulty;
    ProtectedRegion region;
    RegionManager regions;
    WorldGuard wg;

    //Block data
    BlockData redConc = Material.RED_CONCRETE.createBlockData();
    BlockData yellowConc = Material.YELLOW_CONCRETE.createBlockData();
    BlockData limeConc = Material.LIME_CONCRETE.createBlockData();

    public ClaimEnter(PlotSystem plugin, PlotSQL plotSQL, GlobalSQL globalSQl) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plotSQL = plotSQL;
        this.globalSQL = globalSQl;

        plotOutline = new PlotOutline();

        wg = WorldGuard.getInstance();
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {

        User u = PlotSystem.getInstance().getUser(e.getPlayer());
        checkRegion(u);
        u.last_outline_check = e.getPlayer().getLocation();
        checkLocation(u);

    }

    @EventHandler
    public void moveEvent(PlayerMoveEvent e) {
        User u = PlotSystem.getInstance().getUser(e.getPlayer());
        checkRegion(u);
        checkLocation(u);
    }

    @EventHandler
    public void teleportEvent(PlayerTeleportEvent e) {
        User u = PlotSystem.getInstance().getUser(e.getPlayer());
        checkRegion(u);
        checkLocation(u);
    }

    public void checkRegion(User u) {

        Location l = u.player.getLocation();

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet applicableRegionSet = query.getApplicableRegions(BukkitAdapter.adapt(l));

        for (ProtectedRegion regions : applicableRegionSet) {
            if (regions.contains(BlockVector3.at(l.getX(), l.getY(), l.getZ()))) {
                try {

                    int plot = tryParse(regions.getId());

                    if (plot == 0) {
                        continue;
                    }

                    if (u.inPlot != plot) {

                        //If the plot is claimed, send the relevant message.
                        if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + ";")) {

                            //Set the claimed value to false to indicate the plot is not claimed.
                            u.isClaimed = false;
                            u.plotOwner = false;
                            u.plotMember = false;
                            u.player.sendActionBar(Component.text("You have entered plot " + plot + ", it is currently unclaimed.", NamedTextColor.GREEN));
                            u.player.sendActionBar(Component.text("Open the building menu and click on the emerald to claim the plot.", NamedTextColor.GREEN));

                        } else {

                            //Set the claimed value to true to indicate the plot is already claimed.
                            u.isClaimed = true;

                            //If you are the owner of the plot send the relevant message.
                            if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + u.uuid + "' AND is_owner=1;")) {

                                u.plotOwner = true;
                                u.plotMember = false;
                                plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "';");
                                u.player.sendActionBar(Component.text("You have entered plot " + plot + ", you are the owner of this plot.", NamedTextColor.GREEN));

                                //If you are a member of the plot send the relevant message.
                            } else if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + u.uuid + "' AND is_owner=0;")) {

                                u.plotOwner = false;
                                u.plotMember = true;
                                plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "';");
                                u.player.sendActionBar(Component.text("You have entered plot " + plot + ", you are a member of this plot.", NamedTextColor.GREEN));

                            } else {

                                //If you are not an owner or member send the relevant message.
                                u.plotOwner = false;
                                u.plotMember = false;
                                u.player.sendActionBar(Component.text("You have entered " +
                                        globalSQL.getString("SELECT name FROM player_data WHERE uuid = " + plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plot + "AND is_owner=1;") + ";")
                                        + "'s plot."));

                            }
                        }

                        u.inPlot = plot;

                    } else {

                        //If you are the owner or member of this plot update your last enter time.
                        if (u.plotMember || u.plotOwner) {

                            plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "';");

                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        if (applicableRegionSet.size() < 1 && u.inPlot != 0) {

            //If the plot is claimed, send the relevant message.
            if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + ";")) {

                u.player.sendActionBar(Component.text("You have left plot " + u.inPlot, NamedTextColor.GREEN));

            } else {

                //If you are the owner of the plot send the relevant message.
                if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "' AND is_owner=1;")) {

                    u.plotOwner = false;
                    u.player.sendActionBar(Component.text("You have left your plot", NamedTextColor.GREEN));

                    //If you are a member of the plot send the relevant message.
                } else if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "' AND is_owner=0;")) {

                    u.plotMember = false;
                    u.player.sendActionBar(Component.text("You have left " +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + u.inPlot + " AND is_owner=1;") + "';")
                            + "'s plot."));

                } else {

                    //If you are not an owner or member send the relevant message.
                    u.player.sendActionBar(Component.text("You have left " +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid='" + plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + u.inPlot + " AND is_owner=1;") + "';")
                            + "'s plot."));

                }
            }

            u.inPlot = 0;
            u.isClaimed = true;

        }
    }

    public int tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public void checkLocation(User u) {

        if (u.last_outline_check.getWorld() == null) {

            updateOutlines(u);
            return;

        }

        if (u.last_outline_check.getWorld().equals(u.player.getWorld())) {

            //If the player is over 100 blocks from the previous check update outlines.
            if (u.last_outline_check.distance(u.player.getLocation()) > 50) {
                updateOutlines(u);
            }

        } else {
            updateOutlines(u);
        }
    }

    public void updateOutlines(User u) {

        //Get regions.
        regions = wg.getPlatform().getRegionContainer().get(BukkitAdapter.adapt(u.last_outline_check.getWorld()));

        if (regions == null) {return;}

        //Update last outline check and create outlines for all plots in 100 block area around the player.
        u.last_outline_check = u.player.getLocation();

        region = new ProtectedCuboidRegion("test",
                BlockVector3.at(u.last_outline_check.getX() - 100, -60, u.last_outline_check.getZ() - 100),
                BlockVector3.at(u.last_outline_check.getX() + 100, 320, u.last_outline_check.getZ() + 100));
        ApplicableRegionSet set = regions.getApplicableRegions(region);

        for (ProtectedRegion protectedRegion : set) {

            plotID = tryParse(protectedRegion.getId());

            //Get plot difficulty.
            difficulty = plotSQL.getInt("SELECT difficulty FROM plot_data WHERE id=" + plotID + ";");

            plotOutline.createPlotOutline(u.player, plotID, difficultyMaterial(difficulty));

        }
    }

    //Returns the plot difficulty material.
    public BlockData difficultyMaterial(int difficulty) {

        return switch (difficulty) {
            case 1 -> limeConc;
            case 2 -> yellowConc;
            case 3 -> redConc;
            default -> null;
        };
    }
}