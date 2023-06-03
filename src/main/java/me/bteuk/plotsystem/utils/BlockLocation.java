package me.bteuk.plotsystem.utils;

import lombok.Getter;
import org.bukkit.block.data.BlockData;

//Helper class to close x,z locations of blocks
public class BlockLocation {

    @Getter
    private final int x;
    @Getter
    private final int z;

    @Getter
    public final BlockData block;

    public BlockLocation(int x, int z, BlockData block) {
        this.x = x;
        this.z = z;
        this.block = block;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof BlockLocation bl) {
            return(this.x == bl.getX() && this.z == bl.getZ());
        }
        return false;
    }
}
