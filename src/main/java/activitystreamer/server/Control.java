package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;


import activitystreamer.messages.AuthenticateMsg;
import activitystreamer.messages.AuthenticationFail;
import activitystreamer.messages.InvalidMsg;
import activitystreamer.util.Settings;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Control extends Thread {
    private static final Logger log = LogManager.getLogger(Control.class);
    private static ArrayList<Connection> connections;
//    private static ArrayList<Connection> serverConnections;
    private static boolean term = false;
    private static Listener listener;

    protected static Control control = null;

    public static Control getInstance() {
        if (control == null) {
            control = new Control();
        }
        return control;
    }

    public Control() {
        // initialize the connections array
        connections = new ArrayList<Connection>();
        // start a listener
        try {
            listener = new Listener();
        } catch (IOException e1) {
            log.fatal("failed to startup a listening thread: " + e1);
            System.exit(-1);
        }

        try {
            initiateConnection();
        }catch (Exception e1){
            System.out.printf("asdfasdf");
            log.fatal("failed to connect to target server");
            System.exit(-1);
        }
    }

    public void initiateConnection() {
        // make a connection to another server if remote hostname is supplied
        if (Settings.getRemoteHostname() != null) {
            try {
                Socket socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
//                socket.
                Connection con = outgoingConnection(socket,true);
                String msg = AuthenticateMsg.getAuthMsgString(Settings.getSecret());
                con.writeMsg(msg);

            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }
    }

    /*
     * Processing incoming messages from the connection.
     * Return true if the connection should close.
     */
    public synchronized boolean process(Connection con, String msg) {
        boolean terminate = true;
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String command = jsonObject.get("command").toString();
        if (AuthenticateMsg.command.equals(command)) {
            //TODO if contains other information
            String secret = jsonObject.get("secret").toString();
            boolean success = AuthenticateMsg.auth(secret);
            terminate = !success;
            if (!success){
                log.info("the supplied secret is incorrect: "+secret);
                String authFailMsg = AuthenticationFail.getAuthFailString("the supplied secret is incorrect: "+secret);
                con.writeMsg(authFailMsg);
            }else{
                log.info("server register success");
            }
        }else if ("INVALID_MESSAGE".equals(command)){
            terminate = false;
        }else if("AUTHENTICATION_FAIL".equals(command)){
            //TODO resend authticaiton message?
            con.closeCon();
            terminate = true;
            Control.listener.setTerm(true);
        }else{
            //TODO who to close the connection
            String invalidMsg = InvalidMsg.getInvalidMsgString();
            con.writeMsg(invalidMsg);
            terminate = true;
        }
        return terminate;
    }

    /*
     * The connection has been closed by the other party.
     */
    public synchronized void connectionClosed(Connection con) {
        if (!term) connections.remove(con);
    }

    /*
     * A new incoming connection has been established, and a reference is returned to it
     */
    public synchronized Connection incomingConnection(Socket s,boolean isServer) throws IOException {
        log.debug("incomming connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s,isServer);
        connections.add(c);
        return c;

    }

    /*
     * A new outgoing connection has been established, and a reference is returned to it
     */
    public synchronized Connection outgoingConnection(Socket s,boolean isServer) throws IOException {
        log.debug("outgoing connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s,isServer);
        connections.add(c);
        return c;

    }

    @Override
    public void run() {
        log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
        while (!term) {
            // do something with 5 second intervals in between
            try {
                Thread.sleep(Settings.getActivityInterval());
            } catch (InterruptedException e) {
                log.info("received an interrupt, system is shutting down");
                break;
            }
            if (!term) {
                log.debug("doing activity");
                term = doActivity();
            }

        }
        log.info("closing " + connections.size() + " connections");
        // clean up
        for (Connection connection : connections) {
            connection.closeCon();
        }
        listener.setTerm(true);
    }
    public boolean sendAuthenticateMsg(AuthenticateMsg authenticateMsg){

        return false;
    }

    public boolean doActivity() {
        return false;
    }

    public final void setTerm(boolean t) {
        term = t;
    }

    public final ArrayList<Connection> getConnections() {
        return connections;
    }
}
