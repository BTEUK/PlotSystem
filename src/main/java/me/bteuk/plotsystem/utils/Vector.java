package me.bteuk.plotsystem.utils;

import static java.lang.Math.abs;
import static java.lang.Math.max;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import com.sk89q.worldedit.math.BlockVector3;

public class Vector {
	
	BlockVector3 p1;
	BlockVector3 p2;
	
	public Vector(BlockVector3 p1, BlockVector3 p2) {
		
		this.p1 = p1;
		this.p2 = p2;
		
	}
	
	public boolean equals(BlockVector3 p3, BlockVector3 p4) {
		
		if (p1.distance(p3) == 0 && p2.distance(p4) == 0) {
			return true;
		} else if (p1.distance(p4) == 0 && p2.distance(p3) == 0) {
			return true;
		} else {
			return false;
		}
		
	}
	
	public ArrayList<Location> vectorBlocks() {

		int diffX = p2.getX()-p1.getX();
		int diffZ = p2.getZ()-p1.getZ();
		
		double divider = max(abs(diffX), abs(diffZ));
		double minX = p1.getX()+0.5;
		double minZ = p1.getZ()+0.5;
		
		ArrayList<Location> line = new ArrayList<Location>();
		Location l;
		
		for (int i = 1; i < divider; i++) {
			
			l = new Location(Bukkit.getWorld("tutorialWorld"), minX + i * (diffX/divider), p1.getY(), minZ + i * (diffZ/divider));
			line.add(l);
			
		}
		
		return line;		
		
	}

}
