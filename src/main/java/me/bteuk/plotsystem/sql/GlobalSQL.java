package me.bteuk.plotsystem.sql;

import me.bteuk.plotsystem.utils.enums.Role;
import me.bteuk.plotsystem.utils.Time;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class GlobalSQL {

    DataSource dataSource;

    public GlobalSQL(DataSource dataSource) {

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


    public boolean playerExists(String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT uuid FROM player_data WHERE uuid = ?;"
        )) {

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();

            return results.next();

        } catch (SQLException sql) {
            sql.printStackTrace();
            return false;
        }
    }

    public boolean updatePlayerName(String uuid, String name) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE player_data SET name = ? WHERE uuid = ?;"
        )) {

            statement.setString(1, name);
            statement.setString(2, uuid);
            statement.executeUpdate();
            return true;

        } catch (SQLException sql) {
            sql.printStackTrace();
            return false;
        }
    }

    public void createPlayerInstance(String uuid, String name, Role role) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO player_data(uuid, name, role, last_online, last_submit) VALUES(?, ?, ?, ?, ?);"
        )) {

            statement.setString(1, uuid);
            statement.setString(2, name);
            statement.setString(3, role.name());
            statement.setLong(4, Time.currentTime());
            statement.setLong(5, 0);
            statement.executeUpdate();

        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    public void updateTime(String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE player_data SET last_online = ? WHERE uuid = ?;"
        )) {

            statement.setLong(1, Time.currentTime());
            statement.setString(2, uuid);
            statement.executeUpdate();

        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    public Role getRole(String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT role FROM player_data WHERE uuid = ?;"
        )) {

            statement.setString(1, uuid);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return Role.valueOf(results.getString("role"));
            } else {
                return Role.GUEST;
            }

        } catch (SQLException sql) {
            sql.printStackTrace();
            return Role.GUEST;
        }
    }

    public void newSubmit(String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE player_data SET last_submit = ? WHERE uuid = ?;"
        )) {

            statement.setLong(1, Time.currentTime());
            statement.setString(2, uuid);
            statement.executeUpdate();

        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    public long getSubmit(String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT last_submit FROM player_data WHERE uuid = ?;"
        )) {

            statement.setString(1, uuid);

            ResultSet results = statement.executeQuery();
            results.next();
            return (results.getLong("last_submit"));

        } catch (SQLException sql) {
            sql.printStackTrace();
            return 0;
        }

    }

    public String getName(String uuid) {

        if (uuid == null) {
            return "null";
        }

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM player_data WHERE uuid = ?;"
        )) {

            statement.setString(1, uuid);

            ResultSet results = statement.executeQuery();
            results.next();
            return (results.getString("name"));

        } catch (SQLException sql) {
            sql.printStackTrace();
            return null;
        }
    }

    public void updateRole(String uuid, Role role) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE player_data SET role = ? WHERE uuid = ?;"
        )) {

            statement.setString(1, role.name());
            statement.setString(2, uuid);
            statement.executeUpdate();

        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    public String getUUID(String name) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT uuid FROM player_data WHERE name = ?;"
        )) {

            statement.setString(1, name);
            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return (results.getString("uuid"));

            } else {
                return null;
            }

        } catch (SQLException sql) {
            sql.printStackTrace();
            return null;
        }
    }
}
