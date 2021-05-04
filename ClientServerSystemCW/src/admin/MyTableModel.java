package admin;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * This is a table model class. It stores the table that
 * user is currently displaying.
 *
 * @author Maksymilian Ćwirzeń
 */
class MyTableModel extends AbstractTableModel {

    private String[] columnNames = {"Empty Table Model"};
    private final ArrayList<String[]> fData = new ArrayList<>();

    /**
     * Read the message from server. Populate our JTable data-structure.
     */
    protected void loadFromDatabase(String[][] data, String[] header) {
        this.fData.clear(); // clear data structure

        this.setColumnNames(header);
        this.fData.addAll(Arrays.asList(data));

        // call the fire table data and structure changed to get the swing gui to refresh the jtable data
        fireTableStructureChanged();
        fireTableDataChanged();
    }

    /**
     * @return Number of rows in the table.
     */
    @Override
    public int getRowCount() {
        return fData.size();
    }

    /**
     * @return Number of columns in the table.
     */
    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    /**
     * @param rowIndex      Index of the row.
     * @param columnIndex   Index of the column.
     * @return              Value of the stored cell.
     */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return fData.get(rowIndex)[columnIndex];
    }

    /**
     * @param columnIndex   Index of the column.
     * @return              The column name at the index.
     */
    @Override
    public String getColumnName(int columnIndex) {
        return columnNames[columnIndex];
    }

    /**
     * @param rowIndex      Index of the row.
     * @param columnIndex   Index of the column.
     * @return              True if the cell is editable, false if it is not.
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return true;
    }

    /**
     * @param aValue        Value to be stored in this cell.
     * @param rowIndex      Index of the row.
     * @param columnIndex   Index of the column.
     */
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        fData.get(rowIndex)[columnIndex] = aValue.toString();
    }

    /**
     * This function returns the list of integers that represent
     * the first column of each table.
     * <p>
     * It serves the purpose of verification
     *
     * @return The first column of the table
     */
    protected ArrayList<Integer> getIDColumn() {
        ArrayList<Integer> IDColumn = new ArrayList<>();
        for (int i = 0; i < getRowCount(); i++) {
            IDColumn.add(Integer.parseInt((String) getValueAt(i, 0)));
        }
        return IDColumn;
    }

    /**
     * Set headers
     *
     * @param columnNames   Names of the columns.
     */
    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
}