package me.bteuk.plotsystem.sql;

import me.bteuk.plotsystem.PlotSystem;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.sql.*;
import java.util.HashMap;

public class GlobalSQL {

    private final BasicDataSource dataSource;
    private int success;

    public GlobalSQL(BasicDataSource dataSource) {

        this.dataSource = dataSource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
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

    //Get a hashmap of all events for this server for the PlotSystem plugin.
    public HashMap<String, String> getEvents(String serverName, HashMap<String, String> map) {

        //Try and get all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT uuid,event FROM server_events WHERE server='" + serverName + "' AND type='plotsystem';");
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                map.put(results.getString(1), results.getString(2));

            }
        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        }

        //Try and delete all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("DELETE FROM server_events WHERE server='" + serverName + "' AND type='plotsystem';")) {

            statement.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return map;
        }

        //Return the map.
        return map;

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

    public long getLong(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getLong(1);

            } else {

                return 0;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    //Update a row in the database, return true if it was successful.
    public boolean update(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            success = statement.executeUpdate();

            //If the insert was successful return true;
            if (success > 0) {return true;}
            else {

                Bukkit.getLogger().warning("SQL update " + sql + " failed!");
                return false;

            }

        } catch (SQLException e) {
            //If for some reason an error occurred in the sql then return false.
            e.printStackTrace();
            return false;
        }
    }

    //Add new coordinate to database and return the id.
    public int addCoordinate(Location l) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO coordinates(server,world, x, y, z, yaw, pitch) VALUES(?, ?, ?, ?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        )) {
            statement.setString(1, PlotSystem.SERVER_NAME);
            statement.setString(2, l.getWorld().getName());
            statement.setDouble(3, l.getX());
            statement.setDouble(4, l.getY());
            statement.setDouble(5, l.getZ());
            statement.setFloat(6, l.getYaw());
            statement.setFloat(7, l.getPitch());
            statement.executeUpdate();

            //If the id does not exist return 0.
            ResultSet results = statement.getGeneratedKeys();
            if (results.next()) {

                return results.getInt(1);

            } else {

                return 0;

            }

        } catch (SQLException sql) {

            sql.printStackTrace();
            return 0;

        }
    }
}
