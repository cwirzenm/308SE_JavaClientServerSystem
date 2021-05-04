package server;

import both.*;
import java.io.*;
import java.net.Socket;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.BufferedReader;
import java.io.PrintWriter;

/**
 * This is our thread class with the responsibility of handling client requests
 * once the client has connected. A socket is stored to allow connection.
 * <p>
 * There are two ways to make a thread, one is to extend from the Thread class.
 * The other way is to implement the Runnable interface. Implementing Runnable
 * is better because we do not have to waste our inheritance option.
 *
 * @author Chris Bass, modified by Maksymilian Ćwirzeń
 */
class AdminHandlerThread implements Runnable {

    private final Socket socket;
    private final SQLiteDatabase database;

    private final PrintWriter printWriter;
    private final BufferedReader bufferedReader;

    private static int connectionCount = 0;
    private final int connectionNumber;
    protected String tableDisplayed = null;

    /**
     * Constructor just initialises the connection to client.
     *
     * @param socket        The socket to establish the connection to client.
     * @param database      The link to the SQLiteDatabase class.
     * @throws IOException  If an I/O error occurs when creating the input and
     *                      output streams, or if the socket is closed, or socket is not connected.
     */
    protected AdminHandlerThread(Socket socket, SQLiteDatabase database) throws IOException {
        this.socket = socket;
        this.database = database;

        printWriter = new PrintWriter(socket.getOutputStream(), true);
        bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        connectionCount++;
        connectionNumber = connectionCount;
        printWriter.println("You are client number" + connectionNumber);
        threadSays("Connection " + connectionNumber + " established.");
    }

    /**
     * The run method is overridden from the Runnable interface. It is called
     * when the Thread is in a 'running' state - usually after thread.start()
     * is called. This method reads client requests and processes names until
     * an exception is thrown.
     */
    @Override
    public void run() {
        try {
            // Read and process names until an exception is thrown.
            threadSays("Waiting for data from client...");
            String lineRead;
            while ((lineRead = bufferedReader.readLine()) != null) {
                Parcel parcel = new Parcel();
                parcel.unpackParcel(lineRead.split(";"));
                threadSays("Read data from client: \"" + lineRead + "\".");

                String replyMessage = null;

                switch (parcel.getCommand()) {

                    case ADD -> {
                        replyMessage = "Executing ADD on '" + parcel.getTable() + "'";

                        // execute sql query
                        database.commandAdd(parcel.getTable(), parcel.getData());

                        // update the database for all users
                        ThreadedServer.synchronise();
                    }

                    case DELETE -> {
                        replyMessage = "Executing DELETE on the line number " + parcel.getSelect() +
                                " of the '" + parcel.getTable() + "' table.";

                        // execute sql query
                        database.commandDelete(parcel.getTable(), parcel.getSelect());

                        // update the database for all users
                        ThreadedServer.synchronise();
                    }
                    case EDIT -> {
                        replyMessage = "Executing EDIT on the line number " + parcel.getSelect() +
                                " of the '" + parcel.getTable() + "' table.";

                        // execute sql query
                        database.commandEdit(parcel.getTable(), parcel.getData(), parcel.getSelect());

                        // update the database for all users
                        ThreadedServer.synchronise();
                    }
                    case GET -> {
                        replyMessage = "Executing GET on '" + parcel.getTable() + "'";
                        this.tableDisplayed = parcel.getTable();

                        // execute sql query
                        database.commandSync(parcel.getTable());
                        getUpdatedTable(parcel.getTable());
                    }
                    default -> System.out.println("Error: Parcel Error");
                }
                printWriter.println(replyMessage);
            }
        } catch (IOException ex) {
            Logger.getLogger(AdminHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                threadSays("We have lost connection to client " + connectionNumber + ".");
                ThreadedServer.removeThread(this);
                socket.close();
            } catch (IOException ex) {
                Logger.getLogger(AdminHandlerThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * This method recognises the table, gets it from the database
     * and sends it back to Admin.
     *
     * @param table Table to be sent to Admin.
     */
    protected void getUpdatedTable(String table) {
        if (table != null) {
            switch (table) {
                case "odds" -> {
                    database.commandSync(table);
                    printWriter.println("SQL_TABLE" + database.oddsTableList);
                }
                case "bookies" -> {
                    database.commandSync(table);
                    printWriter.println("SQL_TABLE" + database.bookiesTableList);
                }
                case "football-match-data" -> {
                    database.commandSync(table);
                    printWriter.println("SQL_TABLE" + database.matchTableList);
                }
                default -> System.out.println("Error: Table Error");
            }
        }
    }

    /**
     * This method sends the datetime broadcast to Admin.
     */
    protected void sendBroadcast() {
        printWriter.println("SERVER_BROADCAST: " + new Date());
    }

    /**
     * Private helper method outputs to standard output stream for debugging.
     *
     * @param say the String to write to standard output stream.
     */
    private void threadSays(String say) {
        System.out.println("ClientHandlerThread " + connectionNumber + ": " + say);
    }
}