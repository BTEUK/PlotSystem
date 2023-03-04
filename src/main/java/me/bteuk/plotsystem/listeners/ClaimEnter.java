package me.bteuk.plotsystem.listeners;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.bteuk.network.utils.Time;
import me.bteuk.network.utils.Utils;
import me.bteuk.plotsystem.PlotSystem;
import me.bteuk.plotsystem.sql.GlobalSQL;
import me.bteuk.plotsystem.sql.PlotSQL;
import me.bteuk.plotsystem.utils.User;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

public class ClaimEnter implements Listener {

    PlotSQL plotSQL;
    GlobalSQL globalSQL;

    public ClaimEnter(PlotSystem plugin, PlotSQL plotSQL, GlobalSQL globalSQl) {

        Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
        this.plotSQL = plotSQL;
        this.globalSQL = globalSQl;

    }


    @EventHandler
    public void joinEvent(PlayerJoinEvent e) {

        Bukkit.getScheduler().scheduleSyncDelayedTask(PlotSystem.getInstance(), () -> {
            User u = PlotSystem.getInstance().getUser(e.getPlayer());
            checkRegion(u);
        }, 20L);
    }

    @EventHandler
    public void moveEvent(PlayerMoveEvent e) {
        User u = PlotSystem.getInstance().getUser(e.getPlayer());
        checkRegion(u);
    }

    @EventHandler
    public void teleportEvent(PlayerTeleportEvent e) {
        User u = PlotSystem.getInstance().getUser(e.getPlayer());
        //Delay this so the teleport has taken place.
        Bukkit.getScheduler().runTask(PlotSystem.getInstance(), () -> checkRegion(u));
    }

    public void checkRegion(User u) {

        Location l = u.player.getLocation();

        RegionQuery query = WorldGuard.getInstance().getPlatform().getRegionContainer().createQuery();
        ApplicableRegionSet applicableRegionSet = query.getApplicableRegions(BukkitAdapter.adapt(l));

        for (ProtectedRegion regions : applicableRegionSet) {
            try {

                //Get plot or zone.
                String region = regions.getId();

                //Try for a plot.
                int plot = tryParse(region);

                //Try for a zone.
                int zone = tryParse(region.replace("z", ""));

                //If plot is not 0.
                if (plot != 0) {
                    checkPlot(u, plot);
                }

                //If zone is not 0.
                if (zone != 0) {
                    checkZone(u, plot);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //If you're current in a plot or zone, but you're not in a region, show the player that they left it.
        if (applicableRegionSet.size() < 1 && (u.inPlot + u.inZone) > 0) {

            //If the plot is claimed, send the relevant message.
            if (u.inPlot != 0) {
                if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + ";")) {

                    u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(Utils.success("You have left plot &3" + u.inPlot)));

                } else {

                    //If you are the owner of the plot send the relevant message.
                    if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "' AND is_owner=1;")) {

                        u.plotOwner = false;
                        u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(Utils.success("You have left your plot")));

                    } else {

                        //If you are a member of the plot unset plotMember.
                        if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "' AND is_owner=0;")) {
                            u.plotMember = false;
                        }

                        //If you are not an owner or member send the relevant message.
                        u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                                TextComponent.fromLegacyText(Utils.success("You have left &3" + globalSQL.getString("SELECT name FROM player_data WHERE uuid = '" +
                                        plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + u.inPlot + " AND is_owner=1;") + "';") + "'s &aplot.")));

                    }
                }

                u.inPlot = 0;
                u.isClaimed = true;

            } else if (u.inZone != 0) {

                //Show zone leave message.
                u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(Utils.success("You have left zone &3" + u.inPlot)));
                u.inZone = 0;

            }
        }
    }

    private int tryParse(String text) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private void checkPlot(User u, int plot) {

        if (u.inPlot != plot) {

            //If the plot is claimed, send the relevant message.
            if (!plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + ";")) {

                //Set the claimed value to false to indicate the plot is not claimed.
                u.isClaimed = false;
                u.plotOwner = false;
                u.plotMember = false;
                u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(Utils.success("You have entered plot &3" + plot + "&a, it is unclaimed.")));

            } else {

                //Set the claimed value to true to indicate the plot is already claimed.
                u.isClaimed = true;

                //If you are the owner of the plot send the relevant message.
                if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + u.uuid + "' AND is_owner=1;")) {

                    u.plotOwner = true;
                    u.plotMember = false;
                    plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "';");
                    u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(Utils.success("You have entered plot &3" + plot + "&a, you are the owner of this plot.")));

                    //If you are a member of the plot send the relevant message.
                } else if (plotSQL.hasRow("SELECT id FROM plot_members WHERE id=" + plot + " AND uuid='" + u.uuid + "' AND is_owner=0;")) {

                    u.plotOwner = false;
                    u.plotMember = true;
                    plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "';");
                    u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(Utils.success("You have entered plot &3" + plot + "&a, you are a member of this plot.")));

                } else {

                    //If you are not an owner or member send the relevant message.
                    u.plotOwner = false;
                    u.plotMember = false;
                    u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                            TextComponent.fromLegacyText(Utils.success("You have entered &3" + globalSQL.getString("SELECT name FROM player_data WHERE uuid = '" +
                                    plotSQL.getString("SELECT uuid FROM plot_members WHERE id=" + plot + " AND is_owner=1;") + "';") + "'s &aplot.")));

                }
            }

            u.inPlot = plot;

        } else {

            //If you are the owner or member of this plot update your last enter time.
            if (u.plotMember || u.plotOwner) {

                plotSQL.update("UPDATE plot_members SET last_enter=" + Time.currentTime() + " WHERE id=" + u.inPlot + " AND uuid='" + u.uuid + "';");

            }
        }
    }

    private void checkZone(User u, int zone) {

        if (u.inZone != zone) {

            //Check if the zone is public.
            if (plotSQL.hasRow("SELECT id FROM zones WHERE id=" + zone + " AND is_public=1;")) {

                u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(Utils.success("You have entered zone &3" + zone + "&a, it is public.")));

            } else {

                u.player.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                        TextComponent.fromLegacyText(Utils.success("You have entered zone &3" + zone)));

            }

            u.inZone = zone;

        }
    }
}