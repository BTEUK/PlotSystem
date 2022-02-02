package me.bteuk.plotsystem.utils;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class Particles {

	public static void spawnRedParticles(Player p, Location l) {

		p.spawnParticle(Particle.REDSTONE, l, 10, new Particle.DustOptions(Color.RED, 3));

	}

	public static void spawnGreenParticles(Player p, Location l) {

		p.spawnParticle(Particle.REDSTONE, l, 10, new Particle.DustOptions(Color.YELLOW, 3));
		
	}

	public static void spawnBlueParticles(Player p, Location l) {

		p.spawnParticle(Particle.REDSTONE, l, 10, new Particle.DustOptions(Color.BLUE, 3));

	}

}
