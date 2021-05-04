
// Before testing the program, please modify the path of the database in ConnectionFactory.java
// The database has been slightly modified:
// In the football-match-data table, a column 'match_id' has been added
// The database is included in the project directory and in GitHub

package admin;

import both.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is our Admin class with the GUI and all Admin functionalities.
 * Upon launch, Admin connects to a Thread, which is an intermediary
 * between Admin and Server
 *
 * @author Maksymilian Ćwirzeń
 */
class Admin extends JFrame {

    private int adminNumber = 0;
    private final Object waitObject = new Object();
    private final MyTableModel sqlTableModel = new MyTableModel();
    private Parcel parcel = new Parcel();

    private JPanel mainPanel;
    private JTable tableData;
    private JButton buttonConnect;
    private JButton buttonExecute;
    private JTextArea labelStatus;
    private JTextArea labelDatetime;
    private JTextField textFieldData;
    private JTextField textFieldSelect;
    private JTextField textFieldCommand;
    private JComboBox comboBoxTables;

    private BufferedReader bufferedReader;
    private PrintWriter printWriter;
    private Socket socket;

    /**
     * This is our Admin constructor. It consists of all GUI elements.
     */
    private Admin() {
        super();
        this.add(mainPanel);
        this.setTitle("Simple Client");
        labelStatus.setText("Status: Initialising GUI");

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e); //To change body of generated methods, choose Tools | Templates.
                closeConnection();
                System.exit(0);
            }
        });
        tableData.setModel(sqlTableModel);
        tableData.setVisible(true);
        buttonConnect.addActionListener(e -> reconnectToServer());
        buttonExecute.addActionListener(e -> {
            if (socket != null) {
                switch (textFieldCommand.getText()) {
                    case "ADD" -> sendToServer(Commands.ADD);           // ADD
                    case "DELETE" -> sendToServer(Commands.DELETE);     // DELETE
                    case "EDIT" -> sendToServer(Commands.EDIT);         // EDIT
                    case "GET" -> sendToServer(Commands.GET);           // GET
                    default -> adminSays("Invalid Command! Commands available: [ADD, DELETE, EDIT, GET].");
                }
            } else adminSays("You must first connect to server!");
        });
        this.pack();
        this.setVisible(true);
        this.setAlwaysOnTop(true);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }

    /**
     * Send the queries to server
     * Function is executed when the 'Execute' button is pressed on the GUI.
     *
     * @param command Command to be executed
     */
    private void sendToServer(Commands command) {
        if (printWriter != null && bufferedReader != null) {

            parcel.setCommand(command);
            parcel.setTable(Objects.requireNonNull(comboBoxTables.getSelectedItem()).toString());
            boolean flag = true;

            // 1. read data from data field
            if (command == Commands.ADD
                    || command == Commands.EDIT) {
                // VERIFY data from data field
                parcel.setData(textFieldData.getText());

                flag = verifyTextFields(false);
            }

            // 2. read data from select field
            if (command == Commands.DELETE
                    || command == Commands.EDIT) {
                // VERIFY data from select field
                parcel.setSelect(textFieldSelect.getText());

                flag = verifyTextFields(true);
            }

            // if verification is successful, send the data to the server
            if (flag) {
                adminSays("Sending " + parcel + " to server.");
                System.out.println(parcel);
                printWriter.println(parcel);
            }

            // reset the parcel
            parcel = new Parcel();

        } else {
            adminSays("You must connect to the server first!");
        }
    }

    /**
     * Setup connection to the server on the loop back address and the same port
     * number as the Server is expecting.
     */
    private void reconnectToServer() {
        closeConnection();
        adminSays("Attempting connection to server");
        try {
            socket = new Socket("127.0.0.1", 2000);
            printWriter = new PrintWriter(socket.getOutputStream(), true);
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            adminSays("Connected to server");
        } catch (IOException ex) {
            Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
            adminSays("Server not found"); // connection failed
        }
        // notify that the connection is back
        synchronized (waitObject) {
            waitObject.notify();
        }
    }

    /**
     * Keep reading for messages from the server and updating the GUI.
     */
    private void keepReadingFromServer() {
        //noinspection InfiniteLoopStatement
        while (true) {

            // if we have lost connection then just pause this loop until we
            // receive notification to start running again.
            if (socket == null) {
                adminSays("Waiting for connection to be reset...");
                synchronized (waitObject) {
                    try {
                        waitObject.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

            String reply = null;
            try {
                reply = bufferedReader.readLine();
            } catch (IOException ex) {
                adminSays("IOException " + ex);
            }
            if (reply != null && reply.startsWith("You are client number")) {
                adminNumber = Integer.parseInt(reply.substring(21));
                adminSays(reply);
            } else if (reply != null && reply.startsWith("SERVER_BROADCAST")) {
                String datetime = reply.substring(17);
                labelDatetime.setText(datetime);
            } else if (reply != null && reply.startsWith("SQL_TABLE")) {
                System.out.println("Table: " + reply.substring(10));
                readTableData(reply.substring(10));
            } else {
                adminSays("Received \"" + reply + "\" from server.");
            }
        }
    }

    /**
     * Read and extract the table data from the server.
     *
     * @param table Whole table compressed to a single string.
     */
    private void readTableData(String table) {
        String[] stringRead = table.split("},");

        // extracting columns headers
        String[] columnTop = stringRead[0].split("\\{")[1].split("=");
        String[] columnNames = new String[columnTop.length - 1];
        for (int i = 1; i < columnTop.length - 1; i++) {
            columnTop[i] = columnTop[i].split(", ")[1];
        }
        System.arraycopy(columnTop, 0, columnNames, 0, columnTop.length - 1);

        // extracting rows
        String[][] data = new String[stringRead.length][columnNames.length];
        for (int i = 0; i < stringRead.length; i++) {
            String[] oneRow = stringRead[i].split("\\{")[1].split("=");
            for (int j = 1; j < columnNames.length; j++) {
                oneRow[j] = oneRow[j].split(", ")[0];
            }
            System.arraycopy(oneRow, 1, data[i], 0, columnNames.length);
        }

        // fixing the last element of the table
        data[stringRead.length - 1][columnNames.length - 1] =
                data[stringRead.length - 1][columnNames.length - 1].split("}")[0];

        // updating the table
        sqlTableModel.loadFromDatabase(data, columnNames);
        tableData.setModel(sqlTableModel);
    }

    /**
     * Function that verifies the user inputs
     *
     * @param isSelectField if true then verifying the select field, if false then verifying the data field
     * @return Returns a flag that either allows or prevents the data to be sent further
     */
    private boolean verifyTextFields(boolean isSelectField) {

        int tableWidth;
        if (parcel.getTable().equals("odds")) tableWidth = OddsTable.getNumberOfHeaders();
        else if (parcel.getTable().equals("bookies")) tableWidth = BookiesTable.getNumberOfHeaders();
        else tableWidth = MatchTable.getNumberOfHeaders();

        // verifying the selection field
        if (isSelectField) {
            if (parcel.getSelect() != null) { // if is not null
                if (parcel.getSelect().equals("LAST")) {
                    return true;
                } else {
                    try {
                        if (!sqlTableModel.getIDColumn().contains(Integer.parseInt(parcel.getSelect()))) {
                            adminSays("""
                                    This row doesn't exist.\s
                                    You can type 'LAST' if you\s
                                    want to select a last element""");
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        adminSays("Error. Select field isn't an integer" +
                                "\nor 'LAST'");
                        return false;
                    }
                }
            } else {
                adminSays("Error. Select field is null");
                return false;
            }
        }

        // verifying the data field
        else {
            if (parcel.getData() != null) { // if is not null
                String[] dataField = parcel.getData().split(",");
                if (dataField.length == tableWidth) {
                    for (String x : dataField) {
                        try {
                            Integer.parseInt(x);
                        } catch (NumberFormatException e) {
                            if (!(x.startsWith("'") && x.endsWith("'"))) {
                                adminSays("""
                                        Error. Data format invalid.
                                        Don't make a space after comma
                                        Surround the Strings with single parenthesis [']""");
                                return false;
                            }
                        }
                    }
                } else {
                    adminSays("Error. Data field invalid." +
                            "\nUse commas to separate values" +
                            "\nYou need " + tableWidth + " values for this table");
                    return false;
                }
            } else {
                adminSays("Error. Data field is null");
                return false;
            }
        }
        return true;
    }

    /**
     * Close the connection to server.
     */
    private void closeConnection() {
        if (socket != null) {
            adminSays("Closing connection");
            try {
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                socket = null;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(Admin.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Private helper method outputs to standard output stream for debugging.
     *
     * @param say the String to write to standard output stream.
     */
    private void adminSays(String say) {
        System.out.println("Admin" + adminNumber + ": " + say);
        labelStatus.setText("Status: " + say);
    }

    /**
     * Our main method that initialises the Admin constructor and listens for information from the server.
     */
    public static void main(String[] args) {
        Admin admin = new Admin();
        admin.keepReadingFromServer();
    }
}
