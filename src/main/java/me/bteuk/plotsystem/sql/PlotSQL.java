package me.bteuk.plotsystem.sql;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.*;
import java.util.ArrayList;

public class PlotSQL {

    private final BasicDataSource dataSource;

    //Set the dataSource for the plot_data database.
    public PlotSQL(BasicDataSource dataSource) {

        this.dataSource = dataSource;

    }

    private Connection conn() throws SQLException {

        return dataSource.getConnection();

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

                    return results.getInt(1);

                } else {

                    return 0;

                }
            }

        } catch (SQLException sql) {

            sql.printStackTrace();
            return 0;

        }

    }

    //Update a row in the database, return true if it was successful.
    public boolean update(String sql) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException e) {
            //If for some reason an error occurred in the sql then return false.
            e.printStackTrace();
            return false;
        }

        return true;
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

    //Return the first string for a specific statement, if no value is found return null.
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

    //Return all ints into a list.
    public ArrayList<Integer> getIntList(String sql) {

        ArrayList<Integer> list = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                list.add(results.getInt(1));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;

    }

    //Return all strings into a list.
    public ArrayList<String> getStringList(String sql) {

        ArrayList<String> list = new ArrayList<>();

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement(sql);
             ResultSet results = statement.executeQuery()) {

            while (results.next()) {

                list.add(results.getString(1));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;

    }

    public int[][] getPlotCorners(int plotID) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT COUNT(corner) FROM plot_corners WHERE id=" + plotID + ";");
             ResultSet results = statement.executeQuery()) {

            results.next();

            int[][] corners = new int[results.getInt(1)][2];

            corners = getPlotCorners(corners, plotID);

            return corners;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private int[][] getPlotCorners(int[][] corners, int plotID) {

        try (Connection conn = conn();
             PreparedStatement statement = conn.prepareStatement("SELECT x,z FROM plot_corners WHERE id=" + plotID + ";");
             ResultSet results = statement.executeQuery()) {

            for (int i = 0; i < corners.length; i++) {

                results.next();
                corners[i][0] = results.getInt(1);
                corners[i][1] = results.getInt(2);

            }

            return corners;

        } catch (SQLException e) {
            e.printStackTrace();
            return corners;
        }
    }
}
