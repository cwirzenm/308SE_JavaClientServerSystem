package both;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * This class represents a single row of data in the database table. *
 *
 * @author Maksymilian Ćwirzeń
 */
public record BookiesTable(int bookie_id, String name, String website) implements Serializable {

    /**
     * This function updates the current table data with the results
     * acquired from the database.
     *
     * @param resultSet The set of results acquired from the SQL query.
     * @return An updated table.
     * @throws SQLException If the table is not compatible.
     */
    public static BookiesTable newBookiesFromResultSet(ResultSet resultSet) throws SQLException {
        return new BookiesTable(
                resultSet.getInt(1),
                resultSet.getString(2),
                resultSet.getString(3));
    }

    /**
     * @return Headers of the editable columns.
     */
    public static String getHeaders() {
        return "(name,website)";
    }

    /**
     * @return Number of editable columns.
     */
    public static int getNumberOfHeaders() {
        return 2;
    }

    /**
     * @return The ID column.
     */
    public static String getID() {
        return "bookie_id";
    }

    /**
     * This is an overridden method from the Serializable interface.
     * It compresses the table to a single string.
     *
     * @return The table compressed to a string.
     */
    @Override
    public String toString() {
        return "Bookies{" +
                "bookie_id=" + bookie_id +
                ", Name=" + name +
                ", Website=" + website +
                '}';
    }
}