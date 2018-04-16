package activitystreamer.server;

import java.io.IOException;
import java.net.Socket;
import java.util.*;


import activitystreamer.connection.Connection;
import activitystreamer.datastructure.User;
import activitystreamer.messages.*;
import activitystreamer.util.Settings;
import activitystreamer.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import com.sun.org.apache.regexp.internal.RE;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Control extends Thread {
    private static final Logger log = LogManager.getLogger(Control.class);
    private static ArrayList<Connection> clientConnections;
    private static ArrayList<Connection> serverConnections;
    private static boolean term = false;
    private static Listener listener;
    private static Set<String> usernameSet = new HashSet<String>();
    private static Map<String,Integer> lockRequestWaitCount = new HashMap<String, Integer>();
    private static Map<String,Connection> lockRequestParentMap = new HashMap<String, Connection>();
    private static Map<String,Connection> registerParentMap = new HashMap<String, Connection>();





    protected static Control control = null;

    public static Control getInstance() {
        if (control == null) {
            control = new Control();
        }
        return control;
    }

    public Control() {
        // initialize the connections array
        clientConnections = new ArrayList<Connection>();
        serverConnections = new ArrayList<Connection>();
        // start a listener

        try {
//            return;
            listener = new Listener();
        } catch (IOException e1) {
            log.fatal("failed to startup a listening thread: " + e1);
            System.exit(-1);
        }

        try {
            initiateConnection();
        } catch (Exception e1) {
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
                Connection con = outgoingConnection(socket, true);
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
        if ("AUTHENTICATE".equals(command)) {
            //TODO if contains other information
            String secret = jsonObject.get("secret").toString();
            boolean success = AuthenticateMsg.auth(secret);
            terminate = !success;
            if (!success) {
                log.info("the supplied secret is incorrect: " + secret);
                String authFailMsg = AuthenticationMsg.getAuthFailString("the supplied secret is incorrect: " + secret);
                con.writeMsg(authFailMsg);
            } else {
                log.info("server register success");
            }
        } else if ("INVALID_MESSAGE".equals(command)) {
            terminate = false;
        } else if ("AUTHENTICATION_FAIL".equals(command)) {
            //TODO resend authticaiton message?
            con.closeCon();
            terminate = true;
            Control.listener.setTerm(true);
            //TODO need to delete
            System.out.println("asdfasfasdfs");
            term = true;
        } else if ("LOGIN".equals(command)) {
            for (Connection con2 : serverConnections) {

            }
        } else if ("LOGIN_SUCCESS".equals(command)) {

        } else if ("REDIRECT".equals(command)) {

        } else if ("LOGIN_FAILED".equals(command)) {

        } else if ("LOGOUT".equals(command)) {

        } else if ("ACTIVITY_MESSAGE".equals(command)) {

        } else if ("SERVER_ANNOUNCE".equals(command)) {

        } else if ("ACTIVITY_BROADCAST".equals(command)) {

        } else if ("REGISTER".equals(command)) {
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                log.info("REGISTER fail because of invalid message");
                con.writeMsg(InvalidMsg.getInvalidMsg());
                return true;
            }
            if (usernameSet.contains(username)) {
                log.info("REGISTER fail because of duplicate in local storage");
                con.writeMsg(RegisterFailedMsg.getRegisterFailedMsg(username));
                return true;
            }

            lockRequestWaitCount.put(User.getUserString(username,secret),0);
            registerParentMap.put(User.getUserString(username,secret),con);
            for (Connection connection : serverConnections) {
                if (con == connection) {
                    continue;
                }
                connection.writeMsg(LockRequestMsg.getLockRequestMsg(username, secret));
            }

        } else if ("REGISTER_FAILED".equals(command)) {

        } else if ("REGISTER_SUCCESS".equals(command)) {

        } else if ("LOCK_REQUEST".equals(command)) {
            terminate = false;
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                log.info("LOCK_REQUEST can't be accept because of message");
                con.writeMsg(InvalidMsg.getInvalidMsg());
                //TODO if invalid message do I need to return lock_dinied
                con.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
                //TODO terminal state
                return false;
            }

            if (usernameSet.contains(username)) {
                log.info("LOCK_REQUEST fail because of duplicate, and return lock_denied msg");
                con.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
                return false;
            } else {
                log.info("LOCK_REQUEST success, and return lock_allowed msg and broadcast lock_request msg");
                if (serverConnections.size() == 1) {
                    con.writeMsg(LockAllowedMsg.getLockAllowedMsg(username, secret));
                    return false;
                }
                lockRequestWaitCount.put(User.getUserString(username,secret),0);
                lockRequestParentMap.put(User.getUserString(username,secret),con);

                for (Connection connection : serverConnections) {
                    if (con == connection) {
                        continue;
                    }
                    connection.writeMsg(LockRequestMsg.getLockRequestMsg(username, secret));
                }
                return false;
            }
        } else if ("LOCK_ALLOWED".equals(command)) {
            terminate = false;
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                con.writeMsg(InvalidMsg.getInvalidMsg());
                //TODO if invalid message do I need to return lock_dinied
                con.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
                //TODO terminal state
                return false;
            }

            int count = lockRequestWaitCount.get(User.getUserString(username,secret));
            count++;
            if(count == serverConnections.size()-1){
                log.info("this server can return lock_allowed");
                String user = User.getUserString(username,secret);
                if(registerParentMap.containsKey(user)){
                    registerParentMap.get(user)
                            .writeMsg(RegisterSucessMsg.getRegisterSucessMsg(username));
                }else{
                    lockRequestParentMap.get(user)
                            .writeMsg(LockAllowedMsg.getLockAllowedMsg(username, secret));
                }

            }
        } else if ("LOCK_DENIED".equals(command)) {
            terminate = false;
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");

            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                con.writeMsg(InvalidMsg.getInvalidMsg());
                con.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
                return false;
            }

            log.info("this server can return lock_allowed");
            String user = User.getUserString(username,secret);
            if(lockRequestParentMap.containsKey(user)){
                lockRequestParentMap.get(user)
                        .writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
            }else{
                registerParentMap.get(user)
                        .writeMsg(RegisterFailedMsg.getRegisterFailedMsg(username));
            }


        } else {
            //TODO who to close the connection
            String invalidMsg = InvalidMsg.getInvalidMsg();
            con.writeMsg(invalidMsg);
            terminate = true;
        }
        return terminate;
    }

    /*
     * The connection has been closed by the other party.
     */
    public synchronized void connectionClosed(Connection con) {
        if (!term) {serverConnections.remove(con);}
    }

    /*
     * A new incoming connection has been established, and a reference is returned to it
     */
    public synchronized Connection incomingConnection(Socket s, boolean isServer) throws IOException {
        log.debug("incomming connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s, isServer);
        if (isServer){
            serverConnections.add(c);
        }else{
            clientConnections.add(c);
        }
        return c;

    }

    /*
     * A new outgoing connection has been established, and a reference is returned to it
     */
    public synchronized Connection outgoingConnection(Socket s, boolean isServer) throws IOException {
        log.debug("outgoing connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s, isServer);
        if(isServer){
            serverConnections.add(c);
        }else{
            clientConnections.add(c);
        }
        return c;

    }

    @Override
    public void run() {
        log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
        while (!term) {
            // do something with 5 second intervals in between
            try {
                //TODO program haven't reached here, amazing.
                System.out.println("------------------------");
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
        log.info("closing " + serverConnections.size() + " server connections");
        log.info("closing " + clientConnections.size() + " client connections");
        // clean up
        for (Connection connection : serverConnections) {
            connection.closeCon();
        }
        for (Connection connection : clientConnections) {
            connection.closeCon();
        }

        listener.setTerm(true);
    }

    public boolean sendAuthenticateMsg(AuthenticateMsg authenticateMsg) {

        return false;
    }

    public boolean doActivity() {
        return false;
    }

    public final void setTerm(boolean t) {
        term = t;
    }

    public final ArrayList<Connection> getServerConnections() {
        return serverConnections;
    }

    public final ArrayList<Connection> getClientConnections() {
        return clientConnections;
    }
}
