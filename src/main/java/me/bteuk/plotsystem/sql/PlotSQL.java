package me.bteuk.plotsystem.sql;

import com.sk89q.worldedit.math.BlockVector2;
import me.bteuk.plotsystem.Main;
import me.bteuk.plotsystem.plots.Location;
import me.bteuk.plotsystem.utils.Time;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

public class PlotSQL {

    private DataSource dataSource;
    private NavigationSQL navigationSQL;
    private int success;

    //Set the dataSource for the plot_data database.
    public PlotSQL(DataSource dataSource, NavigationSQL navigationSQL) {

        this.dataSource = dataSource;
        this.navigationSQL = navigationSQL;

    }

    private Connection conn() throws SQLException {

        return dataSource.getConnection();

    }

    //Returns whether you are able to build in the specified world.
    public boolean buildable(String world) {

        //Create a statement to select the type where name = world.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT type FROM world_data WHERE name=?;"
        )) {

            statement.setString(1, world);

            try (ResultSet results = statement.executeQuery()) {

                //If there is a result for this world, and it is of type build then return true, else return false.
                if (results.next()) {

                    if (results.getString("type").equals("build")) {

                        return true;

                    } else {

                        return false;

                    }

                } else {

                    return false;

                }
            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Returns the name of the save world.
    public String getSaveWorld() {

        //Create a statement to select the type where type = save.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM world_data WHERE server='" + Main.SERVER_NAME + "', type='save';"
        )) {

            try (ResultSet results = statement.executeQuery()) {

                //If there is a value for save the return the name, else return null.
                if (results.next()) {

                    return results.getString("name");

                } else {

                    return null;

                }
            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return null.
            sql.printStackTrace();
            return null;
        }
    }

    //Adds a new world to the database
    public boolean addWorld(String name, String type) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO world_data(name, type, server) VALUES(?, ?, ?);"
        )) {

            statement.setString(1, name);
            statement.setString(2, type);
            statement.setString(3, Main.SERVER_NAME);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Checks whether the plot is claimed.
    public boolean isClaimed(int plotID) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT id FROM plot_members WHERE id=?;"
        )) {

            statement.setInt(1, plotID);

            try (ResultSet results = statement.executeQuery()) {

                if (results.next()) {

                    return true;

                } else {

                    return false;

                }
            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Checks whether you are the plot owner.
    public boolean isOwner(int plotID, String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT id FROM plot_members WHERE id=?, uuid=?, is_owner=?;"
        )) {

            statement.setInt(1, plotID);
            statement.setString(2, uuid);
            statement.setBoolean(3, true);
            ResultSet results = statement.executeQuery();

            if (results.next()) {

                return true;

            } else {

                return false;

            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Checks whether you are the plot owner.
    public boolean isMember(int plotID, String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT id FROM plot_members WHERE id=?, uuid=?, is_owner=?;"
        )) {

            statement.setInt(1, plotID);
            statement.setString(2, uuid);
            statement.setBoolean(3, false);
            ResultSet results = statement.executeQuery();

            if (results.next()) {

                return true;

            } else {

                return false;

            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Get the owner of a plot.
    public String getOwner(int plotID) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT uuid FROM plot_members WHERE id=?, is_owner=?;"
        )) {

            statement.setInt(1, plotID);
            statement.setBoolean(2, true);
            ResultSet results = statement.executeQuery();

            if (results.next()) {

                return results.getString(1);

            } else {

                return null;

            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return null;
        }
    }

    //Checks whether you are the plot owner.
    public boolean updateLastEnter(int plotID, String uuid) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "UPDATE plot_members SET last_enter WHERE id=?, uuid=?;"
        )) {

            statement.setLong(1, Time.currentTime());
            statement.setInt(2, plotID);
            statement.setString(3, uuid);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Get location bounds for specific world + server.
    public ArrayList<Location> getLocations(String world) {

        ArrayList<Location> locations = new ArrayList<>();

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT name, coordMin, coordMax FROM location_data WHERE world=?, server=?;"
        )) {

            statement.setString(1, world);
            statement.setString(2, Main.SERVER_NAME);

            try (ResultSet results = statement.executeQuery()) {

                while (results.next()) {

                    locations.add(new Location(
                            results.getString("name"),
                            (int) navigationSQL.getX(results.getInt("coordMin")),
                            (int) navigationSQL.getX(results.getInt("coordMax")),
                            (int) navigationSQL.getZ(results.getInt("coordMin")),
                            (int) navigationSQL.getZ(results.getInt("coordMax"))
                    ));
                }
            }

        } catch (SQLException sql) {

            //If for some reason an error occurred in the sql then return false.
            sql.printStackTrace();
            return null;
        }

        return locations;
    }

    //Creates a new plot and returns the id of the plot.
    public int createPlot(int size, int difficulty, String location) {

        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO plot_data(status, size, difficulty, location) VALUES(?, ?, ?, ?);",
                Statement.RETURN_GENERATED_KEYS
        )) {

            statement.setString(1, "unclaimed");
            statement.setInt(2, size);
            statement.setInt(3, difficulty);
            statement.setString(4, location);
            statement.executeUpdate();

            //If the id does not exist return 0.
            try (ResultSet results = statement.getGeneratedKeys()) {
                if (results.next()) {

                    return results.getInt("id");

                } else {

                    return 0;

                }
            }

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
            if (success > 0) {return true;}
            else {

                Bukkit.getLogger().warning("SQL insert " + sql + " failed!");
                return false;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
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

                Bukkit.getLogger().warning("SQL insert " + sql + " failed!");
                return false;

            }

        } catch (SQLException e) {
            //If for some reason an error occurred in the sql then return false.
            e.printStackTrace();
            return false;
        }
    }

    //Check whether the database has the specific row, return boolean.
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

    //Return the first int for a specific statement, if no value is found return 0.
    public int getInt(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            if (results.next()) {

                return results.getInt(1);

            } else {

                return 0;

            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
