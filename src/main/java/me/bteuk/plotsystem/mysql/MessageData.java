package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class MessageData {
	
	DataSource dataSource;

	public MessageData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public boolean addMessage(String uuid, int plot, String type) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO message_data(uuid, plot, type) VALUES(?, ?, ?);"
				)){
			
			statement.setString(1, uuid);
			statement.setInt(2, plot);
			statement.setString(3, type);
			statement.executeUpdate();
			
			return true;
			
		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}
	
	public int hasMessage(String uuid) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id FROM message_data WHERE uuid = ?;"
				)){
			
			statement.setString(1, uuid);
			
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getInt("id");
			} else {
				return 0;
			}
			
		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public String getType(int id) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT type FROM message_data WHERE id = ?;"
				)){
			
			statement.setInt(1, id);
			
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getString("type");
			} else {
				return null;
			}
			
		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;
		}
	}
	
	public int getPlot(int id) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT plot FROM message_data WHERE id = ?;"
				)){
			
			statement.setInt(1, id);
			
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return results.getInt("plot");
			} else {
				return 0;
			}
			
		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}
	
	public boolean delete(int id) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM message_data WHERE id = ?;"
				)){
			
			statement.setInt(1, id);
			statement.executeUpdate();
			return true;
			
		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

}
