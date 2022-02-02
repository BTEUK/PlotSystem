package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.sql.DataSource;

import org.bukkit.configuration.file.FileConfiguration;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.Leaderboard;
import me.bteuk.plotsystem.utils.Time;

public class PlayerData {

	DataSource dataSource;
	
	public PlayerData(DataSource dataSource) {
		
		this.dataSource = dataSource;
		
	}
	
	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}
	
	
	public boolean playerExists(String uuid) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM player_data WHERE uuid = ?;"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			if (results.next()) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}


	}

	public boolean updatePlayerName(String uuid, String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE player_data SET name = ? WHERE uuid = ?;"
		)){
			statement.setString(1, name);
			statement.setString(2, uuid);
			statement.executeUpdate();
			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	public void createPlayerInstance(String uuid, String name, String role) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO player_data(uuid, name, b_points, last_join, role, last_submit) VALUES(?, ?, ?, ?, ?, ?);"
		)){
			statement.setString(1, uuid);
			statement.setString(2, name);
			statement.setInt(3, 0);
			statement.setLong(4, Time.currentTime());
			statement.setString(5, role);
			statement.setLong(6, 1);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}
	
	public void updateTime(String uuid) {		

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE player_data SET last_join = ? WHERE uuid = ?;"
		)){
			statement.setLong(1, Time.currentTime());
			statement.setString(2, uuid);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

	public void addPoints(String uuid, int points) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE player_data SET b_points = b_points + " + points + " WHERE uuid = ?;"
		)){
			statement.setString(1, uuid);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

	public int getPoints(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT b_points FROM player_data WHERE uuid = ?;"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			if (results.next()) {
				return (results.getInt("b_points"));
			} else {
				return 0;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	public String getRole(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT role FROM player_data WHERE uuid = ?;"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			if (results.next()) {
				return (results.getString("role"));
			} else {
				return null;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
	}

	public List<String> getInactivePlayers() {

		//Get config.
		FileConfiguration config = Main.getInstance().getConfig();

		//Calculate the time in milliseconds inactiveMax days ago, inactiveMax is the number of days player can be inactive before their plots are cancelled.
		int inactiveMax = config.getInt("plot_inactive_cancel");
		long time = inactiveMax*24*60*60*1000;
		long currentTime = Time.currentTime();
		long timeCheck = currentTime - time;

		//Create list of inactive players.
		List<String> players = new ArrayList<String>();

		//Find all players who have not been online since the timeCheck time.
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM player_data WHERE last_join < ?;"
		)){
			statement.setLong(1, timeCheck);
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				players.add(results.getString("uuid"));
			}

			if (players.isEmpty()) {
				return null;
			} else {
				return players;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}


	}

	public void newSubmit(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE player_data SET last_submit = ? WHERE uuid = ?;"
		)){
			statement.setLong(1, Time.currentTime());
			statement.setString(2, uuid);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

	public long getSubmit(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT last_submit FROM player_data WHERE uuid = ?;"
		)){
			statement.setString(1, uuid);

			ResultSet results = statement.executeQuery();
			results.next();
			return (results.getLong("last_submit"));

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}

	}

	public String getName(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT name FROM player_data WHERE uuid = ?;"
		)){
			statement.setString(1, uuid);

			ResultSet results = statement.executeQuery();
			results.next();
			return (results.getString("name"));

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
	}

	public void updateRole(String uuid, String role) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE player_data SET role = ? WHERE uuid = ?;"
		)){
			statement.setString(1, role);
			statement.setString(2, uuid);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

	public Leaderboard pointsAboveBelow(String uuid, String name) {

		Leaderboard lead = new Leaderboard();

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid, name, b_points FROM player_data ORDER BY b_points DESC;"
		)){
			ResultSet results = statement.executeQuery();

			int pos = 0;

			while (results.next()) {
				pos += 1;
				if (results.getString("uuid").equals(uuid)) {
					break;
				}
			}

			for (int j = 0; j < 5; j++) {
				pos -= 1;
				if (results.previous()) {
				} else {
					break;
				}
			}

			for (int i = 0; i < 9; i++) {

				if (results.next()) {
					pos += 1;
					lead.position[i] = pos;
					lead.uuids[i] = results.getString("name");
					lead.points[i] = results.getInt("b_points");
				} else {
					return lead;
				}

			}

			return lead;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}

	}

	public LinkedHashMap<String,Integer> pointsTop() {

		LinkedHashMap<String,Integer> lead = new LinkedHashMap<String,Integer>();

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT name, b_points FROM player_data ORDER BY b_points DESC;"
		)){
			ResultSet results = statement.executeQuery();

			int i = 1;

			while (results.next()) {

				if (i > 9) { break; }

				lead.put(results.getString("name"), results.getInt("b_points"));
				i += 1;
			}

			return lead;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}

	}

	public String getUUID(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM player_data WHERE name = ?;"
		)){
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();

			if (results.next()) {
				return (results.getString("uuid"));

			} else {
				return null;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
	}

}
