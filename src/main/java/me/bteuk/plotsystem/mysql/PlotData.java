package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import me.bteuk.plotsystem.utils.Time;
import me.bteuk.plotsystem.utils.User;

public class PlotData {

	DataSource dataSource;

	public PlotData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	//Will generate a new id as the lowest unused integer value.
	public int getNewID() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id FROM plot_data;"
				)){
			ResultSet results = statement.executeQuery();

			if (results.last()) {
				int last = results.getInt("id");
				return (last+1);
			} else {
				return 1;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 1;
		}
	}

	//Created a new plot entry in the database.
	public boolean createPlot(int id, String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO plot_data(id, uuid, status, last_enter) VALUES(?, ?, ?, ?);"
				)){
			statement.setInt(1, id);
			statement.setString(2, uuid);
			statement.setString(3, "claimed");
			statement.setLong(4, Time.currentTime());
			statement.executeUpdate();

			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;			
		}
	}

	//Checks whether the player has a plot that is claimed, submitted or under review.
	public boolean hasPlot(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id FROM plot_data WHERE uuid = ? AND (status = ? OR status = ? OR status = ?);"
				)){
			statement.setString(1, uuid);
			statement.setString(2, "claimed");
			statement.setString(3, "submitted");
			statement.setString(4, "reviewing");

			ResultSet results = statement.executeQuery();
			return results.next();

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	//Checks whether there is a submitted plot to review.
	public boolean reviewExists(User u) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM plot_data WHERE status = ? AND uuid <> ?;"
				)){
			statement.setString(1, "submitted");
			statement.setString(2, u.uuid);

			ResultSet results = statement.executeQuery();
			return (results.next());

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	//Counts number of plots available for review
	public int reviewCount(User u) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT COUNT(uuid) FROM plot_data WHERE status = ? AND uuid <> ?;"
				)){
			statement.setString(1, "submitted");
			statement.setString(2, u.uuid);

			ResultSet results = statement.executeQuery();
			results.next();
			return results.getInt(1);


		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	//Changed the status of the specific plot.
	//Statuses options are: claimed, submitted, reviewing, cancelled, completed.
	public void setStatus(int plot, String status) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE plot_data SET status = ? WHERE id = ?;"
				)){
			statement.setString(1, status);
			statement.setInt(2, plot);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}		
	}

	//Changes the status of all plots that are under review back to submitted.
	//This is run on server start to make sure nothing gets stuck.
	public void clearReview() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE plot_data SET status = ? WHERE status = ?;"
				)){
			statement.setString(1, "submitted");
			statement.setString(2, "reviewing");
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}		
	}

	//Selects the lowest id plot and sets it to reviewing.
	//A reviewer will be assigned that plot.
	public int newReview(User u) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id, uuid FROM plot_data WHERE status = ?;"
				)){
			statement.setString(1, "submitted");
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				if (results.getString("uuid").equals(u.uuid)) {
					continue;
				} else {
					int plot = results.getInt("id");

					setStatus(plot, "reviewing");

					return(plot);
				}
			}

			return 0;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}		
	}

	//Gets all plots claimed or submitted by a player and returns them in a hashmap with id and status.
	public HashMap<Integer, String> getPlots(String uuid) {

		HashMap<Integer, String> plots = new HashMap<Integer, String>();

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id, status FROM plot_data WHERE uuid = ? AND (status = ? OR status = ?);"
				)){			
			statement.setString(1, uuid);
			statement.setString(2, "claimed");
			statement.setString(3, "submitted");
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				plots.put(results.getInt("id"), results.getString("status"));
			}

			return plots;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
	}

	//Returns the active plot count, this only includes plots that are claimed.
	public int activePlotCount(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT COUNT(id) FROM plot_data WHERE uuid = ? AND status = ?;"
				)){
			statement.setString(1, uuid);
			statement.setString(2, "claimed");

			ResultSet results = statement.executeQuery();
			results.next();
			return results.getInt(1);

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	//Returns total plot count, this includes plots that are claimed, submitted and under review.
	public int totalPlotCount(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT COUNT(id) FROM plot_data WHERE uuid = ? AND (status = ? OR status = ? OR status = ?);"
				)){
			statement.setString(1, uuid);
			statement.setString(2, "claimed");
			statement.setString(3, "submitted");
			statement.setString(4, "reviewing");

			ResultSet results = statement.executeQuery();
			results.next();
			return results.getInt(1);

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	//Returns completed plot count.
	public int completedPlots(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT COUNT(id) FROM plot_data WHERE uuid = ? AND status = ?;"
				)){
			statement.setString(1, uuid);
			statement.setString(2, "completed");

			ResultSet results = statement.executeQuery();
			results.next();
			return results.getInt(1);

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	//Returns the uuid of the plot owner.
	public String getOwner(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT uuid FROM plot_data WHERE id = ?;"
				)){
			statement.setInt(1, plot);

			ResultSet results = statement.executeQuery();
			results.next();
			return results.getString("uuid");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}

	}

	//Returns a list of plots that are inactive.
	//Inactive implies that the owner of the plot has not been online in at least 14 days.
	public List<Integer> getInactivePlots(long time) {

		//Create list for the inactive plots.
		List<Integer> plots = new ArrayList<Integer>();

		//Get all plots that are owned by players that are inactive, only active plots will be counted, not submitted ones.
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id FROM plot_data WHERE last_enter < ? AND status = ?;"
				)){
			statement.setLong(1, time);
			statement.setString(2, "claimed");

			ResultSet results = statement.executeQuery();

			while (results.next()) {
				plots.add(results.getInt("id"));
			}

			return plots;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}

	}

	//Sets the last time the owner entered and left their plot
	public void setLastVisit(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE plot_data SET last_enter = ? WHERE id = ?;"
				)){
			statement.setLong(1, Time.currentTime());
			statement.setInt(2, plot);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}

	}

	/*
	 * The following 2 methods are for a different table
	 * they log submitted to this database so that reviewers
	 * can be notified on all server via a bungeecord plugin.
	 */	

	//Adds a submit notification
	public void newSubmit(int id) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO submit_data(id) VALUES(?);"
				)){
			statement.setInt(1, id);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();			
		}

	}

	//Removed a submit notification
	//Only for when a plot submission is retracted before the message is sent
	public void removeSubmit(int id) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM submit_data WHERE id = ?;"
				)){
			statement.setInt(1, id);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();			
		}

	}

}
