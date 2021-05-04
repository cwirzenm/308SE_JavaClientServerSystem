package both;

import java.io.Serializable;

/**
 * This is a parcel class. It stores all information
 * that Admin sends to Server.
 *
 * @author Maksymilian Ćwirzeń
 */
public class Parcel implements Serializable {
    public String Data = null;
    public String Select = null;
    public Commands Command = null;
    public String Table = null;

    /**
     * @return          Insert data from Admin
     */
    public String getData() {
        return Data;
    }

    /**
     * @param Data      Insert data to set
     */
    public void setData(String Data) {
        this.Data = Data;
    }

    /**
     * @return          Select data from Admin
     */
    public String getSelect() {
        return Select;
    }

    /**
     * @param Select    Selection data to set
     */
    public void setSelect(String Select) {
        this.Select = Select;
    }

    /**
     * @return          Command to execute
     */
    public Commands getCommand() {
        return Command;
    }

    /**
     * @param command   Command to set
     */
    public void setCommand(Commands command) {
        this.Command = command;
    }

    /**
     * @return          Table to work with
     */
    public String getTable() { return Table; }

    /**
     * @param Table     Table to set
     */
    public void setTable(String Table) {
        this.Table = Table;
    }

    /**
     * This method unpacks the compressed parcel.
     *
     * @param arrayRead Compressed parcel in a String array
     */
    public void unpackParcel(String[] arrayRead) {
        if (arrayRead[0].equals("null")) { this.setData(null); }
        else { this.setData(arrayRead[0]); }

        if (arrayRead[1].equals("null")) { this.setSelect(null); }
        else { this.setSelect(arrayRead[1]); }

        this.setCommand(Commands.valueOf(arrayRead[2]));
        this.setTable(arrayRead[3]);
    }

    /**
     * This is an overridden method from the Serializable interface.
     * It compresses the parcel to a single string.
     *
     * @return  The parcel compressed to a string.
     */
    @Override
    public String toString() {
        return Data + ";" + Select + ";" + Command + ";" + Table;
    }
}


