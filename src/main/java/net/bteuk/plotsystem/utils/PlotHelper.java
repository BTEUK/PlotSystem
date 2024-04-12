package net.bteuk.plotsystem.utils;

import lombok.Setter;
import net.bteuk.network.sql.PlotSQL;
import net.bteuk.network.utils.enums.PlotStatus;
import net.bteuk.plotsystem.PlotSystem;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for plot-related actions.
 */
public class PlotHelper {

    @Setter
    private static PlotSQL plotSQL;

    @Setter
    private static List<PlotHologram> holograms = new ArrayList<>();

    /**
     * Initialise the plot helper by setting the relevant variables.
     * @param plotSQL   {@link PlotSQL}
     */
    public static void init(PlotSQL plotSQL) {
        setPlotSQL(plotSQL);
    }

    /**
     * Update the status of a plot, will update any relevant holograms.
     * @param id        the plot id
     * @param status    the plot status
     */
    public static boolean updatePlotStatus(int id, PlotStatus status) {
        if (!plotSQL.update("UPDATE plot_data SET status='" + status.database_value + "' WHERE id=" + id + ";")) {
            return false;
        }
        // Delay the hologram update until the plot has been completely updated.
        Bukkit.getScheduler().runTask(PlotSystem.getInstance(), () -> {

            // Update the hologram status.
            List<PlotHologram> hologramsToRemove = new ArrayList<>();
            holograms.stream().filter(hologram -> hologram.getPlot() == id).forEach(hologram -> {
                hologram.updatePlotStatus(status);
                // If the hologram is empty, add it to the list of holograms to remove.
                if (hologram.isEmpty()) {
                    hologramsToRemove.add(hologram);
                }
            });
            // Remove any empty holograms.
            holograms.removeAll(hologramsToRemove);
        });
        return true;
    }

    public static void addPlotHologram(PlotHologram plotHologram) {
        holograms.add(plotHologram);
    }

    public static void updatePlotHologram(int plot) {
        holograms.stream().filter(plotHologram -> plotHologram.getPlot() == plot).forEach(PlotHologram::updateLocation);
    }

    public static void addPlayer(Player player) {
        holograms.forEach(hologram -> hologram.setHologramVisibilityForPlayer(player));
    }
}
