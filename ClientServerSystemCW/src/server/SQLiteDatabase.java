package server;

import both.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class is a link between the server and the SQLite database.
 * <p>
 * It is responsible for carrying out SQL commands and extracting the tables.
 *
 * @author Maksymilian Ćwirzeń
 */
class SQLiteDatabase {

    List<OddsTable> oddsTableList;
    List<BookiesTable> bookiesTableList;
    List<MatchTable> matchTableList;

    /**
     * Constructor, containing all database tables.
     *
     * @param oddsTableList     Odds table.
     * @param bookiesTableList  Bookies table.
     * @param matchTableList    Match data table.
     */
    protected SQLiteDatabase(List<OddsTable> oddsTableList, List<BookiesTable> bookiesTableList,
                             List<MatchTable> matchTableList) {
        this.oddsTableList = oddsTableList;
        this.bookiesTableList = bookiesTableList;
        this.matchTableList = matchTableList;
    }

    /**
     * This method updates the table, which then can be
     * extracted and sent further.
     *
     * @param table Table to synchronise.
     */
    protected synchronized void commandSync(String table) {
        switch (table) {
            case "odds" -> {
                String getSQL = "SELECT * FROM \"odds\" ORDER BY odd_id DESC";
                ArrayList<OddsTable> oddsTables = new ArrayList<>();

                try (Connection conn = ConnectionFactory.getConnection(); // auto close the connection object after try
                     PreparedStatement prep = conn.prepareStatement(getSQL)) {

                    prep.executeQuery();
                    ResultSet resultSet = prep.executeQuery();

                    while (resultSet.next()) {
                        oddsTables.add(OddsTable.newOddsFromResultSet(resultSet));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(SQLiteDatabase.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.oddsTableList = oddsTables;
            }
            case "bookies" -> {
                String getSQL = "SELECT * FROM \"bookies\" ORDER BY bookie_id DESC";
                ArrayList<BookiesTable> bookiesTables = new ArrayList<>();

                try (Connection conn = ConnectionFactory.getConnection(); // auto close the connection object after try
                     PreparedStatement prep = conn.prepareStatement(getSQL)) {

                    prep.executeQuery();
                    ResultSet resultSet = prep.executeQuery();

                    while (resultSet.next()) {
                        bookiesTables.add(BookiesTable.newBookiesFromResultSet(resultSet));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(SQLiteDatabase.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.bookiesTableList = bookiesTables;
            }
            case "football-match-data" -> {
                String getSQL = "SELECT * FROM \"football-match-data\" ORDER BY match_id DESC LIMIT 50;";
                ArrayList<MatchTable> matchTables = new ArrayList<>();

                try (Connection conn = ConnectionFactory.getConnection(); // auto close the connection object after try
                     PreparedStatement prep = conn.prepareStatement(getSQL)) {

                    prep.executeQuery();
                    ResultSet resultSet = prep.executeQuery();

                    while (resultSet.next()) {
                        matchTables.add(MatchTable.newMatchFromResultSet(resultSet));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(SQLiteDatabase.class.getName()).log(Level.SEVERE, null, ex);
                }
                this.matchTableList = matchTables;
            }
        }
    }

    /**
     * This method executes the 'ADD' command on the SQL table.
     * <p>
     * It adds another row of data to a given table.
     *
     * @param table         Table to modify.
     * @param insertData    Data to insert to the table.
     */
    protected synchronized void commandAdd(String table, String insertData) {
        String headers = null;
        switch (table) {
            case "odds" -> headers = OddsTable.getHeaders();
            case "bookies" -> headers = BookiesTable.getHeaders();
            case "football-match-data" -> headers = MatchTable.getHeaders();
            default -> System.out.println("Table Header Error");
        }
        String insertSQL = "INSERT INTO \"" + table + "\" " + headers +
                " VALUES (" + insertData + ");";

        try (Connection conn = ConnectionFactory.getConnection(); // auto close the connection object after try
             PreparedStatement prep = conn.prepareStatement(insertSQL)) {

            prep.execute();

        } catch (SQLException ex) {
            Logger.getLogger(SQLiteDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method executes the 'DELETE' command on the SQL table.
     * <p>
     * It deletes a row of data in a given table.
     *
     * @param table         Table to modify.
     * @param selectData    Row to delete.
     */
    protected synchronized void commandDelete(String table, String selectData) {
        String id = null;
        switch (table) {
            case "odds" -> id = OddsTable.getID();
            case "bookies" -> id = BookiesTable.getID();
            case "football-match-data" -> id = MatchTable.getID();
            default -> System.out.println("Table ID Error");
        }
        String deleteSQL = "DELETE FROM \"" + table + "\" WHERE \"" + id + "\" =" + selectData;
        if (selectData.equals("LAST")) {
            // DELETE FROM odds WHERE odd_id=(SELECT MAX(odd_id) FROM odds)
            deleteSQL = "DELETE FROM \"" + table + "\" WHERE \"" + id + "\"=(SELECT MAX(\"" + id + "\") FROM \"" + table + "\")";
        }

        try (Connection conn = ConnectionFactory.getConnection(); // auto close the connection object after try
             PreparedStatement prep = conn.prepareStatement(deleteSQL)) {

            prep.execute();

        } catch (SQLException ex) {
            Logger.getLogger(SQLiteDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * This method executes the 'EDIT' command on the SQL table.
     * <p>
     * It substitutes a row of data with different data in a given table.
     *
     * @param table         Table to modify.
     * @param insertData    Data to insert to the table.
     * @param selectData    Row to edit.
     */
    protected synchronized void commandEdit(String table, String insertData, String selectData) {
        String id = null;
        String updateSQL = null;
        String[] setData = insertData.split(",");

        switch (table) {
            case "odds" -> {
                id = OddsTable.getID();
                updateSQL = "UPDATE \"" + table + "\"" +
                        " SET bookie_id=" + setData[0] + ", match_id=" + setData[1] +
                        ", odds_home_to_win=" + setData[2] + ", odd_draw=" + setData[3] +
                        ", odds_away_to_win=" + setData[4];
            }
            case "bookies" -> {
                id = BookiesTable.getID();
                updateSQL = "UPDATE \"" + table + "\"" +
                        " SET name=" + setData[0] + ", website=" + setData[1];
            }
            case "football-match-data" -> {
                id = MatchTable.getID();
                updateSQL = "UPDATE \"" + table + "\"" +
                        " SET Season=" + setData[0] + ", Datetime=" + setData[1] +
                        ", Div=" + setData[2] + ", Country=" + setData[3] +
                        ", League=" + setData[4] + ", Referee=" + setData[5] +
                        ", HomeTeam=" + setData[6] + ", AwayTeam=" + setData[7] +
                        ", FTHG=" + setData[8] + ", FTAG=" + setData[9] +
                        ", FTR=" + setData[10] + ", HTHG=" + setData[11] +
                        ", HTAG=" + setData[12];
            }
            default -> System.out.println("Table ID Error");
        }
        if (selectData.equals("LAST")) {
            updateSQL = updateSQL + " WHERE " + id + "=(SELECT MAX(\"" + id + "\") FROM \"" + table + "\")";
        }
        else {
            updateSQL = updateSQL + " WHERE " + id + "=" + selectData;
        }
//        System.out.println(updateSQL);

        try (Connection conn = ConnectionFactory.getConnection(); // auto close the connection object after try
             PreparedStatement prep = conn.prepareStatement(updateSQL)) {

            prep.execute();

        } catch (SQLException ex) {
            Logger.getLogger(SQLiteDatabase.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}