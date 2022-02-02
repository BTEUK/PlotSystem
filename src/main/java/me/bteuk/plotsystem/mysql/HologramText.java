package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.sql.DataSource;

public class HologramText {

	DataSource dataSource;

	public HologramText(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public boolean addLine(String name, int line, String text) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO hologram_text(hologram_name, line, text) VALUES(?, ?, ?);"
				)){
			statement.setString(1, name);
			statement.setInt(2, line);
			statement.setString(3, text);
			statement.executeUpdate();

			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}

	}

	public int lines(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT COUNT(id) FROM hologram_text WHERE hologram_name = ?;"
				)){
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();
			if (results.next()) {
				return (results.getInt(1));
			} else {
				return 0;
			}

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	public boolean hasLine(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT hologram_name FROM hologram_text WHERE hologram_name = ?;"
				)){
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();
			return (results.next());

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}

	}
	
	public boolean hasLine(String name, int line) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT hologram_name FROM hologram_text WHERE hologram_name = ? AND line = ?;"
				)){
			statement.setString(1, name);
			statement.setInt(2, line);
			ResultSet results = statement.executeQuery();
			return (results.next());

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}

	}
	
	public boolean updateLine(String name, int line, String text) {
		
		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE hologram_text SET text = ? WHERE hologram_name = ? AND line = ?;"
				)){
			statement.setString(1, text);
			statement.setString(2, name);
			statement.setInt(3, line);
			statement.executeUpdate();
			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

	public void removeLine(String name) {

		int line = lastLine(name);

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM hologram_text WHERE hologram_name = ? AND line = ?;"
				)){
			statement.setString(1, name);
			statement.setInt(2, line);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

	public int lastLine(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT line FROM hologram_text WHERE hologram_name = ? ORDER BY line DESC;"
				)){
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();
			results.next();

			return (results.getInt("line"));

		} catch (SQLException sql) {
			sql.printStackTrace();
			return 1;			
		}
	}

	public ArrayList<String> getLines(String name) {

		ArrayList<String> lines = new ArrayList<String>();

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT text FROM hologram_text WHERE hologram_name = ? ORDER BY line ASC;"
				)){
			statement.setString(1, name);
			ResultSet results = statement.executeQuery();

			while (results.next()) {
				lines.add(results.getString("text"));
			}

			return lines;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return null;			
		}

	}

	public void deleteLines(String name) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"DELETE FROM hologram_text WHERE hologram_name = ?;"
				)){
			statement.setString(1, name);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

}
