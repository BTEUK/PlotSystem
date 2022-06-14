package me.bteuk.plotsystem.sql;

import java.sql.*;

import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;

public class NavigationSQL {

    private final BasicDataSource dataSource;
    private int success;

    public NavigationSQL(BasicDataSource dataSource) {

        this.dataSource = dataSource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }

    //Add new coordinate to database and return the id.
    public int addCoordinate(Location l) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO coordinates(world, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, l.getWorld().getName());
            statement.setDouble(2, l.getX());
            statement.setDouble(3, l.getY());
            statement.setDouble(4, l.getZ());
            statement.setFloat(5, l.getYaw());
            statement.setFloat(6, l.getPitch());
            statement.executeUpdate();

            //If the id does not exist return 0.
            ResultSet results = statement.getGeneratedKeys();
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

    //Generic update statement, return true is successful.
    public boolean update(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            success = statement.executeUpdate();

            //If the insert was successful return true;
            if (success > 0) {
                return true;
            } else {

                Bukkit.getLogger().warning("SQL insert " + sql + " failed!");
                return false;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Generic statement checking whether a specific row exists.
    public boolean hasRow(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            return results.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getString(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getString(1);

            } else {

                return null;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
