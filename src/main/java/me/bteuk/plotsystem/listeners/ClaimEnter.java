package me.bteuk.plotsystem.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class ClaimEnter implements Listener {

    PlotSQL plotSQL;
    GlobalSQL globalSQL;

    public ClaimEnter(Main plugin, PlotSQL plotSQL, GlobalSQL globalSQl) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plotSQL = plotSQL;
        this.globalSQL = globalSQl;
    }

    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {

        User u = Main.getInstance().getUser(e.getPlayer());
        checkRegion(u);

    }

    //If the player is in a buildable world check whether they enter or exit a plot.
    @EventHandler
    public void moveEvent(PlayerMoveEvent e) {
        User u = Main.getInstance().getUser(e.getPlayer());
        if (plotSQL.buildable(u.player.getWorld().getName())) {
            checkRegion(u);
        }
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
                        if (!plotSQL.isClaimed(plot)) {

                            u.plotOwner = false;
                            u.plotMember = false;
                            u.player.sendActionBar(Component.text("You have entered plot " + plot + ", it is currently unclaimed.", NamedTextColor.GREEN));
                            u.player.sendActionBar(Component.text("Open the building menu and click on the emerald to claim the plot.", NamedTextColor.GREEN));

                        } else {

                            //If you are the owner of the plot send the relevant message.
                            if (plotSQL.isOwner(plot, u.uuid)) {

                                u.plotOwner = true;
                                u.plotMember = false;
                                plotSQL.updateLastEnter(u.inPlot, u.uuid);
                                u.player.sendActionBar(Component.text("You have entered plot " + plot + ", you are the owner of this plot.", NamedTextColor.GREEN));

                                //If you are a member of the plot send the relevant message.
                            } else if (plotSQL.isMember(plot, u.uuid)) {

                                u.plotOwner = false;
                                u.plotMember = true;
                                plotSQL.updateLastEnter(u.inPlot, u.uuid);
                                u.player.sendActionBar(Component.text("You have entered plot " + plot + ", you are a member of this plot.", NamedTextColor.GREEN));

                            } else {

                                //If you are not an owner or member send the relevant message.
                                u.plotOwner = false;
                                u.plotMember = false;
                                u.player.sendActionBar(Component.text("You have entered " +
                                        globalSQL.getString("SELECT name FROM player_data WHERE uuid = " + plotSQL.getOwner(plot) + ";")
                                        + "'s plot."));

                            }
                        }

                        u.inPlot = plot;

                    } else {

                        //If you are the owner or member of this plot update your last enter time.
                        if (u.plotMember || u.plotOwner) {

                            plotSQL.updateLastEnter(u.inPlot, u.uuid);

                        }

                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        if (applicableRegionSet.size() < 1 && u.inPlot != 0) {

            //If the plot is claimed, send the relevant message.
            if (!plotSQL.isClaimed(u.inPlot)) {

                u.player.sendActionBar(Component.text("You have left plot " + u.inPlot, NamedTextColor.GREEN));

            } else {

                //If you are the owner of the plot send the relevant message.
                if (plotSQL.isOwner(u.inPlot, u.uuid)) {

                    u.plotOwner = false;
                    u.player.sendActionBar(Component.text("You have left your plot", NamedTextColor.GREEN));

                    //If you are a member of the plot send the relevant message.
                } else if (plotSQL.isMember(u.inPlot, u.uuid)) {

                    u.plotMember = false;
                    u.player.sendActionBar(Component.text("You have left " +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid = " + plotSQL.getOwner(u.inPlot) + ";")
                            + "'s plot."));

                } else {

                    //If you are not an owner or member send the relevant message.
                    u.player.sendActionBar(Component.text("You have left " +
                            globalSQL.getString("SELECT name FROM player_data WHERE uuid = " + plotSQL.getOwner(u.inPlot) + ";")
                            + "'s plot."));

                }
            }

            u.inPlot = 0;

        }
    }

    public static int tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}