package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import me.bteuk.plotsystem.utils.User;

public class TutorialData {

	DataSource dataSource;

	public TutorialData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public void createPlayerInstance(String uuid, int tutorial_type, int tutorial_stage, boolean first_time) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO tutorial_data(uuid, type, stage, first_time) VALUES(?, ?, ?, ?)"
		)){
			statement.setString(1, uuid);
			statement.setInt(2, tutorial_type);
			statement.setInt(3, tutorial_stage);
			statement.setBoolean(4, first_time);
			statement.executeUpdate();

		} catch (SQLException sql) {
			sql.printStackTrace();
		}
	}

	public boolean tutorialComplete(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT type FROM tutorial_data WHERE uuid = ?"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			if (results.next()) {

				if (results.getInt("type") == 10) {
					return true;
				} else {
					return false;
				}

			} else {
				createPlayerInstance(uuid, 1, 1, true);
				return false;
			}


		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}


	}

	public int getType(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT type FROM tutorial_data WHERE uuid = ?"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			results.next();
			return (results.getInt("type"));


		} catch (SQLException sql) {
			sql.printStackTrace();
			return 1;
		}

	}

	public int getStage(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT stage FROM tutorial_data WHERE uuid = ?"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			results.next();
			return (results.getInt("stage"));


		} catch (SQLException sql) {
			sql.printStackTrace();
			return 1;
		}

	}

	public boolean getTime(String uuid) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT first_time FROM tutorial_data WHERE uuid = ?"
		)){
			statement.setString(1, uuid);
			ResultSet results = statement.executeQuery();

			results.next();
			return (results.getBoolean("first_time"));


		} catch (SQLException sql) {
			sql.printStackTrace();
			return true;
		}

	}

	public void updateValues(User u) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"UPDATE tutorial_data SET type = ?, stage = ?, first_time = ? WHERE uuid = ?"
		)){
			statement.setInt(1, u.tutorial.tutorial_type);
			statement.setInt(2, u.tutorial.tutorial_stage);
			statement.setBoolean(3, u.tutorial.first_time);
			statement.setString(4, u.uuid);

			statement.executeUpdate();


		} catch (SQLException sql) {
			sql.printStackTrace();
		}

	}

}
