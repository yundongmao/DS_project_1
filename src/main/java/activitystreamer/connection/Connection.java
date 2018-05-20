package activitystreamer.connection;


import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


import activitystreamer.client.ClientSkeleton;
import activitystreamer.server.Control;
import activitystreamer.util.Settings;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;


public class Connection extends Thread {
    private static final Logger log = LogManager.getLogger(Connection.class);
    private boolean isServer = false;
    private DataInputStream in;
    private DataOutputStream out;
    private BufferedReader inreader;
    private PrintWriter outwriter;
    private boolean open = false;
    private Socket socket;
    private boolean term = false;

    //TODO I change it public I don't know if it's true
    public Connection(Socket socket, boolean isServer) throws IOException {
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
        inreader = new BufferedReader(new InputStreamReader(in));
        outwriter = new PrintWriter(out, true);
        this.socket = socket;
        this.isServer = isServer;
        open = true;
        start();
    }

    /*
     * returns true if the message was written, otherwise false
     */
    public boolean writeMsg(String msg) {
        if (open) {
            outwriter.println(msg);
            outwriter.flush();
            return true;
        }
        return false;
    }

    public void closeCon() {
        if (open) {
            log.info("closing connection " + Settings.socketAddress(socket));
            try {
                term = true;
                in.close();
//                inreader.close();
//                out.close();
//                open = false;
            } catch (IOException e) {
                // already closed?
                log.error("received exception closing the connection " + Settings.socketAddress(socket) + ": " + e);
            }
        }
    }


    public void run() {
        try {
            String data;
            while (!term && (data = inreader.readLine()) != null) {
                if (Settings.isServer()) {
                    term = Control.getInstance().process(this, data);
                } else {
                    term = ClientSkeleton.getInstance().process(this, data);
                }
            }

            log.debug("connection closed to " + Settings.socketAddress(socket));
            if (Settings.isServer()) {
                Control.getInstance().connectionClosed(this);
                in.close();
            } else {
//                socket.close();
//                ClientSkeleton.getInstance().disconnect();
                in.close();
            }
        } catch (IOException e) {
            log.debug("connection " + Settings.socketAddress(socket) + " closed with exception: " + e);
            if (Settings.isServer()) {
                Control.getInstance().connectionClosed(this);
            } else {
                log.info("nothing happend");
            }
        }
        open = false;
    }

    public Socket getSocket() {
        return socket;
    }

    public boolean isOpen() {
        return open;
    }

    public boolean isServer() {
        return isServer;
    }
}
