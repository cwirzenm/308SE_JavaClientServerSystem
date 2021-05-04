package both;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class represents a single row of data in the database table.
 *
 * @author Maksymilian Ćwirzeń
 */
public record MatchTable(int match_id, String season, String datetime, String division,
                         String country, String league, String referee,
                         String home_team, String away_team, int full_time_home_goals,
                         int full_time_away_goals, String full_time_result, int half_time_home_goals,
                         int half_time_away_goals) implements Serializable {

    /**
     * This function updates the current table data with the results
     * acquired from the database.
     *
     * @param resultSet The set of results acquired from the SQL query.
     * @return An updated table.
     * @throws SQLException If the table is not compatible.
     */
    public static MatchTable newMatchFromResultSet(ResultSet resultSet) throws SQLException {
        return new MatchTable(
                resultSet.getInt(1),
                resultSet.getString(2),
                resultSet.getString(3),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(6),
                resultSet.getString(7),
                resultSet.getString(8),
                resultSet.getString(9),
                resultSet.getInt(10),
                resultSet.getInt(11),
                resultSet.getString(12),
                resultSet.getInt(13),
                resultSet.getInt(14));
    }

    /**
     * @return Headers of the editable columns.
     */
    public static String getHeaders() {
        return "(Season,Datetime,Div,Country,League,Referee,HomeTeam,AwayTeam,FTHG,FTAG,FTR,HTHG,HTAG)";
    }

    /**
     * @return Number of editable columns.
     */
    public static int getNumberOfHeaders() {
        return 13;
    }

    /**
     * @return The ID column.
     */
    public static String getID() {
        return "match_id";
    }

    /**
     * This is an overridden method from the Serializable interface.
     * It compresses the table to a single string.
     *
     * @return The table compressed to a string.
     */
    @Override
    public String toString() {
        return "MatchData{" +
                "match_id=" + match_id +
                ", Season=" + season +
                ", Datetime=" + datetime +
                ", Division=" + division +
                ", Country=" + country +
                ", League=" + league +
                ", Referee=" + referee +
                ", Home Team=" + home_team +
                ", Away Team=" + away_team +
                ", FTHG=" + full_time_home_goals +
                ", FTAG=" + full_time_away_goals +
                ", FTR=" + full_time_result +
                ", HTHG=" + half_time_home_goals +
                ", HTAG=" + half_time_away_goals +
                '}';
    }
}