package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.bteuk.plotsystem.utils.CustomHologram;

public class HologramData {
	
	DataSource dataSource;

	public HologramData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public boolean create(String name, Location l, boolean visible) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO hologram_data(name, world, x, y, z, visible) VALUES(?, ?, ?, ?, ?, ?);"
				)){
			statement.setString(1, name);
			statement.setString(2, l.getWorld().getName());
			statement.setDouble(3, l.getX());
			statement.setDouble(4, l.getY());
			statement.setDouble(5, l.getZ());
			statement.setBoolean(6, visible);
			statement.executeUpdate();

			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}

	}

	public boolean nameExists(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT name FROM hologram_data WHERE name = ?;"
				)){
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();

			return (results.next());

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}
	
	public void delete(String name) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM hologram_data where name = ?;"
				)){
			statement.setString(1, name);
			statement.executeUpdate();
		} catch (SQLException sql) {
			sql.printStackTrace();
		}
		
	}
	
	public boolean move(String name, Location l) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE hologram_data SET world = ?, x = ?, y = ?, z = ? WHERE name = ?;"
				)){
			statement.setString(1, l.getWorld().getName());
			statement.setDouble(2, l.getX());
			statement.setDouble(3, l.getY());
			statement.setDouble(4, l.getZ());
			statement.setString(5, name);
			statement.executeUpdate();
			
			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}
	
	public boolean toggleVisibility(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE hologram_data SET visible = 1 - visible WHERE name = ?;"
				)){
			statement.setString(1, name);
			statement.executeUpdate();
			
			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}
	
	public ArrayList<CustomHologram> getHolos() {
		
		ArrayList<CustomHologram> holos = new ArrayList<CustomHologram>();
		CustomHologram holo;
			
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT * FROM hologram_data;"
				)){
			
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				holo = new CustomHologram(results.getString("name"), new Location(
						Bukkit.getWorld(results.getString("world")), 
						results.getDouble("x"), results.getDouble("y"), results.getDouble("z")),
						results.getBoolean("visible"));
				holos.add(holo);
			}
			
			return holos;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
		
	}

}
