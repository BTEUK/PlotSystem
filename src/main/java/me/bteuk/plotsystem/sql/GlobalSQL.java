package me.bteuk.plotsystem.sql;

import me.bteuk.plotsystem.utils.enums.Role;
import me.bteuk.plotsystem.utils.Time;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class GlobalSQL {

    private BasicDataSource dataSource;
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

    //Get a hashmap of all events for this server.
    public HashMap<String, String> getEvents(String server) {

        //Create map.
        HashMap<String, String> map = new HashMap<>();

        //Try and get all events for this server.
        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT uuid,event FROM server_events WHERE server=" + server + ";");
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
             PreparedStatement statement = conn.prepareStatement("DELETE FROM server_events WHERE server=" + server + ";")) {

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
}
