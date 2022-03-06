package me.bteuk.plotsystem.sql;

import java.sql.*;
import java.util.ArrayList;

import me.bteuk.plotsystem.Main;
import org.apache.commons.dbcp2.BasicDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;

import me.bteuk.plotsystem.utils.CustomHologram;

public class NavigationSQL {

    private BasicDataSource dataSource;
    private int success;

    public NavigationSQL(BasicDataSource dataSource) {

        this.dataSource = dataSource;

    }

    private Connection conn() throws SQLException {
        return dataSource.getConnection();
    }

    public boolean create(String name, Location l, boolean visible) {

        //Add the coordinates first.
        int coordinate_id = addCoordinate(l);

        //If the coordinate id is 0 return false since the coordinate was not created correctly.
        if (coordinate_id == 0) {

            return false;

        }

        //Create the hologram.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO hologram_data(name, server, coordinate_id, visible) VALUES(?, ?, ?, ?);"
        )) {

            statement.setString(1, name);
            statement.setString(2, l.getWorld().getName());
            statement.setInt(3, coordinate_id);
            statement.setBoolean(4, visible);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {

            sql.printStackTrace();
            return false;

        }
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

    //Return whether a hologram already exists with this name.
    public boolean nameExists(String name) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM hologram_data WHERE name = ?;"
        )) {
            statement.setString(1, name);
            ResultSet results = statement.executeQuery();

            return (results.next());

        } catch (SQLException sql) {

            sql.printStackTrace();
            return false;

        }
    }

    public boolean delete(String name) {

        if (!deleteCoordinate(getHoloCoordID(name))) {

            return false;

        }

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM hologram_data where name = ?;"
        )) {
            statement.setString(1, name);
            statement.executeUpdate();
            return true;

        } catch (SQLException sql) {

            sql.printStackTrace();
            return false;

        }

    }

    public int getHoloCoordID(String name) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT coordinate_id FROM hologram_data where name = ?;"
        )) {
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

    public boolean deleteCoordinate(int id) {

        //If the coordinate id is 0 return false.
        if (id == 0) {

            return false;

        }

        //Delete the coordinate and return true.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM coordinates where id = ?;"
        )) {
            statement.setInt(1, id);
            statement.executeUpdate();
            return true;

        } catch (SQLException sql) {

            sql.printStackTrace();
            return false;

        }

    }

    public boolean move(String name, Location l) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE hologram_data SET world = ?, x = ?, y = ?, z = ? WHERE name = ?;"
        )) {
            statement.setString(1, l.getWorld().getName());
            statement.setDouble(2, l.getX());
            statement.setDouble(3, l.getY());
            statement.setDouble(4, l.getZ());
            statement.setString(5, name);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {
            sql.printStackTrace();
            return false;
        }
    }

    public boolean toggleVisibility(String name) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE hologram_data SET visible = 1 - visible WHERE name = ?;"
        )) {
            statement.setString(1, name);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {
            sql.printStackTrace();
            return false;
        }
    }

    public ArrayList<CustomHologram> getHolos() {

        ArrayList<CustomHologram> holos = new ArrayList<>();
        CustomHologram holo;

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT * FROM hologram_data;"
        )) {

            ResultSet results = statement.executeQuery();

            while (results.next()) {
                holo = new CustomHologram(results.getString("name"), new Location(
                        Bukkit.getWorld(results.getString("world")),
                        results.getDouble("x"), results.getDouble("y"), results.getDouble("z")),
                        results.getBoolean("visible"));
                holos.add(holo);
            }

            return holos;

        } catch (SQLException sql) {
            sql.printStackTrace();
            return null;
        }

    }

    public boolean addLine(String name, int line, String text) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO hologram_text(hologram_name, line, text) VALUES(?, ?, ?);"
        )) {
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
        )) {
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
        )) {
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
        )) {
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
        )) {
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
        )) {
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
        )) {
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

        ArrayList<String> lines = new ArrayList<>();

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT text FROM hologram_text WHERE hologram_name = ? ORDER BY line ASC;"
        )) {
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
        )) {
            statement.setString(1, name);
            statement.executeUpdate();

        } catch (SQLException sql) {
            sql.printStackTrace();
        }
    }

    //Adds a new server to the database
    public boolean addServer() {

        //Create a statement to select the server name.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO server_data(name, type) VALUES(?, ?);"
        )) {

            statement.setString(1, Main.SERVER_NAME);
            statement.setString(2, "plot");
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {

            //If for some reason an error occures in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Get the x coordinate of a coordinate
    public double getX(int id) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT x FROM coordinates WHERE id = ?;"
        )) {
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();
            results.next();

            return results.getDouble(1);

        } catch (SQLException sql) {
            sql.printStackTrace();
            return 0;
        }

    }

    //Get the z coordinate of a coordinate
    public double getZ(int id) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT z FROM coordinates WHERE id = ?;"
        )) {
            statement.setInt(1, id);
            ResultSet results = statement.executeQuery();
            results.next();

            return results.getDouble(1);

        } catch (SQLException sql) {
            sql.printStackTrace();
            return 0;
        }

    }

    //Generic insert statement, return true is successful.
    public boolean insert(String sql) {

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
}
