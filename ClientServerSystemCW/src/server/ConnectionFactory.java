package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The purpose of this class is to encapsulate connecting to the SQLite database.
 *
 * @author Chris Bass
 */
class ConnectionFactory {

    private static final String DB_URL = "jdbc:sqlite:C:\\Users\\Kimberly Ng\\IdeaProjects" +
            "\\cwirzenmCoursework\\FootballBetting-SQL\\football-match-data.sqlite";

    /**
     * Get a connection to our SQLite database.
     *
     * @return Connection object, remember to close this connection object after
     * using to avoid memory leaks, connection objects are expensive.
     */
    protected static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }
}