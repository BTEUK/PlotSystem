package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class AreaData {

	DataSource dataSource;

	public AreaData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	private int getID() {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"SELECT id FROM area_data;"
				)){
			ResultSet results = statement.executeQuery();

			if (results.last()) {
				int last = results.getInt("id");
				return last+1;
			} else {
				return 1;
			}
		} catch (SQLException sql) {
			sql.printStackTrace();
			return 0;
		}
	}

	public boolean newArea(String name, String type) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO area_data(id, name, type, status) VALUES(?, ?, ?, ?);"
				)){

			statement.setInt(1, getID());
			statement.setString(2, name);
			statement.setString(3, type);
			statement.setString(4, "active");
			statement.executeUpdate();
			
			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}

	}
}
