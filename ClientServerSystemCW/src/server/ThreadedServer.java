package server;

import java.io.*;
import java.util.*;
import java.net.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A threaded server can handle multiple client's requests at the same time via
 * multi-threading. This class has the responsibility for connecting new admins
 * and starting a new thread for each new admin.
 * It is also responsible for synchronisation of the data across all admins.
 *
 * @author Chris Bass, modified by Maksymilian Ćwirzeń
 */
class ThreadedServer {

    private static final SQLiteDatabase database = new SQLiteDatabase(null, null, null);
    private static final HashSet<AdminHandlerThread> ADMIN_HANDLER_THREADS = new HashSet<>();


    /**
     * Wait until a client connects to the server on a port, then establish the
     * connection via a socket object and create a thread to handle requests.
     */
    private static void connectToAdmin() {
        System.out.println("Server: Server starting.");

        try (ServerSocket serverSocket = new ServerSocket(2000)) {

            //noinspection InfiniteLoopStatement
            while (true) {
                System.out.println("Server: Waiting for connecting client...");

                try {
                    Socket socket = serverSocket.accept();
                    AdminHandlerThread adminHandlerThread = new AdminHandlerThread(socket, database);
                    Thread connectionThread = new Thread(adminHandlerThread);
                    connectionThread.start();
                    ADMIN_HANDLER_THREADS.add(adminHandlerThread);
                } catch (IOException ex) {
                    System.out.println("Server: Could not start connection to a client.");
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ThreadedServer.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("Server: Closed down");
        }
    }

    /**
     * Synchronise the tables for all admins.
     */
    protected static void synchronise() {
        for (AdminHandlerThread handler : ADMIN_HANDLER_THREADS) {
            handler.getUpdatedTable(handler.tableDisplayed);
        }
    }

    /**
     * Remove the unused threads.
     *
     * @param threadToRemove Thread to be removed
     */
    protected static void removeThread(AdminHandlerThread threadToRemove) {
        ADMIN_HANDLER_THREADS.remove(threadToRemove);
    }

    /**
     * Send the broadcast to all admins.
     *
     * @throws IOException If Admin disconnects suddenly.
     */
    private static void broadcastToAdmins() throws IOException {
        for (AdminHandlerThread handler : ADMIN_HANDLER_THREADS) {
            handler.sendBroadcast();
        }
    }

    /**
     * This is our main server method. It initialises the timer
     * and sends the broadcast to admins every period.
     * Then it initialises the server, allowing for the threads to
     * be generated and connected to admins.
     */
    public static void main(String[] args) {

        final Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    ThreadedServer.broadcastToAdmins();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 1000);
        // update the database for all admins everytime a command is being executed
        ThreadedServer.connectToAdmin();
    }
}