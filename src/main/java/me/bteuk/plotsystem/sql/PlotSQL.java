package me.bteuk.plotsystem.sql;

import me.bteuk.plotsystem.Main;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

public class PlotSQL {

    DataSource dataSource;

    //Set the dataSource for the plot_data database.
    public PlotSQL(DataSource dataSource) {

        this.dataSource = dataSource;

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
            ResultSet results = statement.executeQuery();

            //If there is a result for this world and it is of type build then return true, else return false.
            if (results.next()) {

                if (results.getString("type").equals("build")) {

                    return true;

                } else {

                    return false;

                }

            } else {

                return false;

            }

        } catch (SQLException sql) {

            //If for some reason an error occures in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Returns whether you are able to build in the specified world.
    public String getSaveWorld() {

        //Create a statement to select the type where type = save.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM world_data WHERE type='save';"
        )) {

            ResultSet results = statement.executeQuery();

            //If there is a value for save the return the name, else return null.
            if (results.next()) {

                return results.getString("name");

            } else {

                return null;

            }

        } catch (SQLException sql) {

            //If for some reason an error occures in the sql then return null.
            sql.printStackTrace();
            return null;
        }
    }

    //Checks whether the server has been added to the config.
    //If not then the server must be setup first before the plugin is fully enabled.
    public boolean serverSetup(String name) {

        //Create a statement to select the server name.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "SELECT name FROM server_data WHERE name=?;"
        )) {

            ResultSet results = statement.executeQuery();

            //If the server exists return true
            return (results.next());

        } catch (SQLException sql) {

            //If for some reason an error occures in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Adds a new world to the database
    public boolean addWorld(String name, String type) {

        //Create a statement to select the server name.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO world_data(name, type, server) VALUES(?, ?, ?);"
        )) {

            statement.setString(1, name);
            statement.setString(2, type);
            statement.setString(3, Main.SERVER_NAME);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {

            //If for some reason an error occures in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }

    //Adds a new server to the database
    public boolean addServer(boolean tutorial_only, boolean plots_only) {

        //Create a statement to select the server name.
        try (Connection conn = conn(); PreparedStatement statement = conn.prepareStatement(
                "INSERT INTO server_data(name, tutorial_only, plots_only) VALUES(?, ?, ?);"
        )) {

            statement.setString(1, Main.SERVER_NAME);
            statement.setBoolean(2, tutorial_only);
            statement.setBoolean(3, plots_only);
            statement.executeUpdate();

            return true;

        } catch (SQLException sql) {

            //If for some reason an error occures in the sql then return false.
            sql.printStackTrace();
            return false;
        }
    }
}
