package activitystreamer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import activitystreamer.util.Settings;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Listener extends Thread {
    private static final Logger log = LogManager.getLogger(Listener.class);
    private ServerSocket serverSocket = null;
    private boolean term = false;
    private int portnum;

    public Listener() throws IOException {
        portnum = Settings.getLocalPort(); // keep our own copy in case it changes later
        serverSocket = new ServerSocket(portnum);
        start();
    }

    @Override
    public void run() {
        log.info("listening for new connections on " + portnum);
        while (!term) {
            Socket clientSocket;
            try {
                clientSocket = serverSocket.accept();
                boolean isServer = true;
                Control.getInstance().incomingConnection(clientSocket, isServer);
            } catch (IOException e) {
                log.info("received exception, shutting down");
                term = true;
            }
        }
        System.exit(0);
    }

    public void setTerm(boolean term) {
        this.term = term;

        if (term) {
            log.info("close the listener because of the term signal");
            interrupt();
            System.exit(0);
        }
    }


}
