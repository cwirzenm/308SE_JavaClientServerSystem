package both;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class represents a single row of data in the database table.
 *
 * @author Maksymilian Ćwirzeń
 */
public record OddsTable(int odd_id, int bookie_id, int match_id, String odds_home_to_win,
                        String odd_draw, String odds_away_to_win) implements Serializable {

    /**
     * This function updates the current table data with the results
     * acquired from the database.
     *
     * @param resultSet The set of results acquired from the SQL query.
     * @return An updated table.
     * @throws SQLException If the table is not compatible.
     */
    public static OddsTable newOddsFromResultSet(ResultSet resultSet) throws SQLException {
        return new OddsTable(
                resultSet.getInt(1),
                resultSet.getInt(2),
                resultSet.getInt(3),
                resultSet.getString(4),
                resultSet.getString(5),
                resultSet.getString(6));
    }

    /**
     * @return Headers of the editable columns.
     */
    public static String getHeaders() {
        return "(bookie_id,match_id,odds_home_to_win,odd_draw,odds_away_to_win)";
    }

    /**
     * @return Number of editable columns.
     */
    public static int getNumberOfHeaders() {
        return 5;
    }

    /**
     * @return The ID column.
     */
    public static String getID() {
        return "odd_id";
    }

    /**
     * This is an overridden method from the Serializable interface.
     * It compresses the table to a single string.
     *
     * @return The table compressed to a string.
     */
    @Override
    public String toString() {
        return "Odds{" +
                "odd_id=" + odd_id +
                ", bookie_id=" + bookie_id +
                ", match_id=" + match_id +
                ", odds_home_to_win=" + odds_home_to_win +
                ", odd_draw=" + odd_draw +
                ", odds_away_to_win=" + odds_away_to_win +
                '}';
    }
}