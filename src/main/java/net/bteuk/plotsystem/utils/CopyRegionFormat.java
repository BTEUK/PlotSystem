package net.bteuk.plotsystem.utils;

import com.sk89q.worldedit.math.BlockVector3;
import org.bukkit.World;

public class CopyRegionFormat {

    public final World copyWorld;
    public final World pasteWorld;

    public final BlockVector3 minPoint;
    public final BlockVector3 maxPoint;

    public final BlockVector3 pasteMinPoint;

    public CopyRegionFormat(World copyWorld, World pasteWorld, BlockVector3 minPoint, BlockVector3 maxPoint, BlockVector3 pasteMinPoint) {

        this.copyWorld = copyWorld;
        this.pasteWorld = pasteWorld;

        this.minPoint = minPoint;
        this.maxPoint = maxPoint;

        this.pasteMinPoint = pasteMinPoint;

    }
}
