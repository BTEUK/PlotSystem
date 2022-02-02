package me.bteuk.plotsystem.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class PointsData {
	
	DataSource dataSource;

	public PointsData(DataSource dataSource) {

		this.dataSource = dataSource;

	}

	private Connection conn() throws SQLException {
		return dataSource.getConnection();
	}

	public boolean addPoint(int plot, int point, int x, int z) {

		try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
				"INSERT INTO points_data(plot, point, x, z) VALUES (?, ?, ?, ?);"
				)){

			statement.setInt(1, plot);
			statement.setInt(2, point);
			statement.setInt(3, x);
			statement.setInt(4, z);
			
			statement.executeUpdate();
			return true;

		} catch (SQLException sql) {
			sql.printStackTrace();
			return false;
		}
	}

}
