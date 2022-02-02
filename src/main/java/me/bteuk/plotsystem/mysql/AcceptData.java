package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.utils.Time;

public class AcceptData {

	DataSource dataSource;

	public AcceptData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public boolean insert(int plot, String uuid, String reviewer, int feedback, int size, int accuracy, int quality, int points) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO accept_data(plot, uuid, reviewer, feedback, size, accuracy, quality, points, time) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);"
				)){
			statement.setInt(1, plot);
			statement.setString(2, uuid);
			statement.setString(3, reviewer);
			statement.setInt(4, feedback);
			statement.setInt(5, size);
			statement.setInt(6, accuracy);
			statement.setInt(7, quality);
			statement.setInt(8, points);
			statement.setLong(9, Time.currentTime());
			statement.executeUpdate();

			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}

	}

	public boolean hasEntry(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT plot FROM accept_data WHERE uuid = ?;"
				)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			return (results.next());

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	public ArrayList<Integer> getLatest(ArrayList<Integer> accept, String uuid){

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT plot FROM accept_data WHERE uuid = ? ORDER BY time DESC;"
				)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				accept.add(results.getInt("plot"));

				if (accept.size() >= 5) {
					return accept;
				}

			}

			return accept;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return accept;
		}

	}

	public boolean hasFeedback(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT feedback FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();

			if (results.getInt("feedback") == 0) {
				return false;
			} else { return true;}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	public boolean isBuilding(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT size FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();

			if (results.getInt("size") == 0) {
				return false;
			} else { return true;}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	public long getTime(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT time FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			return results.getLong("time");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public String getReviewer(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT reviewer FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			return results.getString("reviewer");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
	}
	
	public int getPoints(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT points FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			return results.getInt("points");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public int getSize(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT size FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			return results.getInt("size");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public int getAccuracy(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT accuracy FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			return results.getInt("accuracy");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public int getQuality(int plot) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT quality FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			return results.getInt("quality");

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public ItemStack getBook(int plot) {
		
		BookData bookData = Main.getInstance().bookData;
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta bookMeta = (BookMeta) book.getItemMeta();
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT feedback FROM accept_data WHERE plot = ?;"
				)){
			statement.setInt(1, plot);
			ResultSet results = statement.executeQuery();

			results.next();
			bookMeta.setPages(bookData.getPages(results.getInt("feedback")));
			bookMeta.setTitle("Plot " + plot);
			
			book.setItemMeta(bookMeta);
			return book;			

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
		
	}
}
