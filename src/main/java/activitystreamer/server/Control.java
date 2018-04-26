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
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Control extends Thread {
    private static final Logger log = LogManager.getLogger(Control.class);
    private static ArrayList<Connection> clientConnections;
    private static ArrayList<Connection> serverConnections;
    private static boolean term = false;
    private static Listener listener;
    private static Map<String, Connection> registerClientConnectionMap = new HashMap<String, Connection>();
    private static Map<String, Integer> registerAllowedGetCount = new HashMap<String, Integer>();
    private static Map<String, Integer> registerAllowedTargetCount = new HashMap<String, Integer>();
    private static Map<Connection, User> loggedInUser = new HashMap<Connection, User>();

    private static Map<String, JSONObject> knownServerMap = new HashMap<String, JSONObject>();
    private static Map<String, String> usernameAndSecretMap = new HashMap<String, String>();


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
        start();

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
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String command = jsonObject.get("command").toString();
        if ("AUTHENTICATE".equals(command)) {
            //TODO if contains other information
            String secret = jsonObject.get("secret").toString();
            boolean success = AuthenticateMsg.auth(secret);
            if (!success) {
                log.info("the supplied secret is incorrect: " + secret);
                String authFailMsg = AuthenticationFailMsg.getAuthenticationFailMsg("the supplied secret is incorrect: " + secret);
                con.writeMsg(authFailMsg);
                return true;
            } else {
                log.info("server authenticate success");
                return false;
            }
        } else if ("INVALID_MESSAGE".equals(command)) {
            log.info("get invalid message : " + InvalidMsg.getInvalidMsg());
            return true;
        } else if ("AUTHENTICATION_FAIL".equals(command)) {
            //TODO resend authticaiton message?
            con.closeCon();
            System.out.println("AUTHENTICATION_FAIL");
            Control.listener.setTerm(true);
            //TODO need to delete
//            System.out.println("asdfasfasdfs");
            term = true;
            return true;
        } else if ("LOGIN".equals(command)) {
            // change connection isServer
            serverConnections.remove(con);
            clientConnections.add(con);
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            if ("anonymous".equals(username)) {
                con.writeMsg(LoginSuccessMsg.getLoginSuccessMsg(username));
                return false;
            }
            if (StringUtils.isNullorEmpty(secret) || StringUtils.isNullorEmpty(username)) {
                con.writeMsg(LoginFailedMsg.getLoginFailedMsg());
                return true;
            }

            if (usernameAndSecretMap.containsKey(username)) {
                if (usernameAndSecretMap.get(username).toString().equals(secret)) {
                    //TODO login success message.
                    log.info("login success " + User.getUserString(username, secret));
                    con.writeMsg(LoginSuccessMsg.getLoginSuccessMsg(username));
                    for (String id : knownServerMap.keySet()) {
                        if (clientConnections.size() >= knownServerMap.get(id).getInteger("load") + 2) {
                            log.info("REDIRECT to " + knownServerMap.get(id).toJSONString());
                            knownServerMap.get(id).getString("hostname");
                            con.writeMsg(RedirectMsg.getRedirectMsg(knownServerMap.get(id).getString("hostname")
                                    , knownServerMap.get(id).getInteger("port")));
                            return true;
                        }
                    }
                    loggedInUser.put(con, new User(username, secret));
                    return false;
                } else {
                    log.info("login fail " + User.getUserString(username, secret));
                    con.writeMsg(LoginFailedMsg.getLoginFailedMsg());
                    return true;
                }
            } else {
                con.writeMsg(LoginFailedMsg.getLoginFailedMsg());
                return true;
            }
        } else if ("LOGOUT".equals(command)) {
            log.info("get " + LogoutMsg.getLogoutMsg());
            //just close the connnection
            return true;
        } else if ("ACTIVITY_MESSAGE".equals(command)) {
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            String activity = jsonObject.getString("activity");
            if (!"anonymous".equals(username)) {
                if (StringUtils.isNullorEmpty(secret) || StringUtils.isNullorEmpty(username)) {
                    con.writeMsg(AuthenticationFailMsg.getAuthenticationFailMsg("username or secret is empty"));
                    return true;
                }
                if (!loggedInUser.get(con).getUsername().equals(username) || !loggedInUser.get(con).getSecret().equals(secret)) {
                    log.info("Authenticate fail in when receive ACTIVITY_MESSAGE" + User.getUserString(username, secret) + " ----- the logged in user is :" + loggedInUser.get(con).toJSONString());
                    con.writeMsg(AuthenticationFailMsg
                            .getAuthenticationFailMsg("Authenticate fail in when receive ACTIVITY_MESSAGE"
                                    + User.getUserString(username, secret)));
                    return true;
                }
            }

            log.info("Authenticate success in when receive ACTIVITY_MESSAGE" + User.getUserString(username, secret));
            for (Connection connection : serverConnections) {
                connection.writeMsg(ActivityBroadcastMsg.getActivityBroadcastMsg(activity));
            }

            for (Connection connection : clientConnections) {
                if (connection == con) {
                    continue;
                }
                connection.writeMsg(ActivityBroadcastMsg.getActivityBroadcastMsg(activity));
            }
            return false;
        } else if ("SERVER_ANNOUNCE".equals(command)) {

            String id = jsonObject.getString("id");
            int load = jsonObject.getInteger("load");
            String hostname = jsonObject.getString("hostname");
            int port = jsonObject.getInteger("port");
            knownServerMap.put(id, jsonObject);
            System.out.println(knownServerMap);
            for (Connection connection : serverConnections) {
                if (con == connection) {
                    continue;
                }
                connection.writeMsg(ServerAnnounceMsg.getServerAnnounceMsg(id, load, hostname, port));
            }
            return false;
        } else if ("ACTIVITY_BROADCAST".equals(command)) {
            String activity = jsonObject.getString("activity");
            if (StringUtils.isNullorEmpty(activity) || jsonObject.keySet().size() > 2) {
                log.info(InvalidMsg.getInvalidMsg());
                con.writeMsg(InvalidMsg.getInvalidMsg());
                return true;
            }
            for (Connection connection : serverConnections) {
                if (connection == con) {
                    continue;
                }
                connection.writeMsg(ActivityBroadcastMsg.getActivityBroadcastMsg(activity));
            }

            for (Connection connection : clientConnections) {
                connection.writeMsg(ActivityBroadcastMsg.getActivityBroadcastMsg(activity));
            }
            return false;

        } else if ("REGISTER".equals(command)) {
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                log.info("REGISTER fail because of invalid message");
                con.writeMsg(InvalidMsg.getInvalidMsg());
                return true;
            }
            if (usernameAndSecretMap.containsKey(username)) {
                log.info("REGISTER fail because of duplicate in local storage");
                con.writeMsg(RegisterFailedMsg.getRegisterFailedMsg(username));
                return false;
            }

            if (serverConnections.size() == 0) {
                log.info("REGISTER success");
                con.writeMsg(RegisterSucessMsg.getRegisterSucessMsg(username));
                usernameAndSecretMap.put(username, secret);
                return false;
            }
//            lockRequestWaitCount.put(User.getUserString(username, secret), 0);
            registerAllowedGetCount.put(User.getUserString(username, secret), 0);
            registerAllowedTargetCount.put(User.getUserString(username, secret), knownServerMap.size());
            registerClientConnectionMap.put(User.getUserString(username, secret), con);
            for (Connection connection : serverConnections) {
                connection.writeMsg(LockRequestMsg.getLockRequestMsg(username, secret));
            }
            return false;
        } else if ("LOCK_REQUEST".equals(command)) {
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

            if (usernameAndSecretMap.containsKey(username)) {
                log.info("LOCK_REQUEST fail because of duplicate, and return lock_denied msg");
                con.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
                return false;
            } else {
                log.info("LOCK_REQUEST success, and return lock_allowed msg and broadcast lock_request msg");

                usernameAndSecretMap.put(username, secret);
                con.writeMsg(LockAllowedMsg.getLockAllowedMsg(username, secret));

//                for (Connection connection : serverConnections) {
//                    if (con == connection) {
//                        continue;
//                    }
//                    connection.writeMsg(LockAllowedMsg.getLockAllowedMsg(username, secret));
//                }
//                if (serverConnections.size() == 1) {
//                    con.writeMsg(LockAllowedMsg.getLockAllowedMsg(username, secret));
//                    return false;
//                }

                for (Connection connection : serverConnections) {
                    if (con == connection) {
                        continue;
                    }
                    connection.writeMsg(LockRequestMsg.getLockRequestMsg(username, secret));
                }
                return false;
            }
        } else if ("LOCK_ALLOWED".equals(command)) {
            log.info("get message LOCK_ALLOWED " + jsonObject.toJSONString());
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");
            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                con.writeMsg(InvalidMsg.getInvalidMsg());
                //TODO if invalid message do I need to return lock_dinied
                con.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
                //TODO terminal state
                return true;
            }


            for (Connection connection : serverConnections) {
                if (connection == con) {
                    continue;
                }
                connection.writeMsg(LockAllowedMsg.getLockAllowedMsg(username, secret));
            }

            if (registerClientConnectionMap.containsKey(User.getUserString(username, secret))) {
                int count = registerAllowedGetCount.get(User.getUserString(username, secret));
                registerAllowedGetCount.put(User.getUserString(username, secret), ++count);
                if (count >= registerAllowedTargetCount.get(User.getUserString(username, secret))) {
                    String user = User.getUserString(username, secret);
                    log.info("this server can return register success");
                    registerClientConnectionMap.get(user)
                            .writeMsg(RegisterSucessMsg.getRegisterSucessMsg(username));
                    usernameAndSecretMap.put(username, secret);
                }
            }

            return false;
        } else if ("LOCK_DENIED".equals(command)) {
            String username = jsonObject.getString("username");
            String secret = jsonObject.getString("secret");

            usernameAndSecretMap.remove(username);
            for (Connection connection : serverConnections) {
                if (connection == con) {
                    continue;
                }
                connection.writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
            }


//            log.info("this server can return lock_allowed");
//            String user = User.getUserString(username,secret);
//            if(lockRequestParentMap.containsKey(user)){
//                lockRequestParentMap.get(user)
//                        .writeMsg(LockDeniedMsg.getLockDeniedMsg(username, secret));
//            }else{
//                registerClientConnectionMap.get(user)
//                        .writeMsg(RegisterFailedMsg.getRegisterFailedMsg(username));
//            }
            return false;

        } else {
            //TODO who to close the connection
            String invalidMsg = InvalidMsg.getInvalidMsg();
            con.writeMsg(invalidMsg);
            return true;
        }
    }

    /*
     * The connection has been closed by the other party.
     */
    public synchronized void connectionClosed(Connection con) {
        if (!term) {
            serverConnections.remove(con);
            clientConnections.remove(con);
            loggedInUser.remove(con);
        }
    }

    /*
     * A new incoming connection has been established, and a reference is returned to it
     */
    public synchronized Connection incomingConnection(Socket s, boolean isServer) throws IOException {
        log.debug("incomming connection: " + Settings.socketAddress(s));
        Connection c = new Connection(s, isServer);
        if (isServer) {
            serverConnections.add(c);
        } else {
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
        if (isServer) {
            serverConnections.add(c);
        } else {
            clientConnections.add(c);
        }
        return c;

    }

    @Override
    public void run() {
        log.info("using activity interval of " + Settings.getActivityInterval() + " milliseconds");
        while (!term) {
            // do something with 5 second intervals in between
            log.info("server announce");
            for (Connection connection : serverConnections) {
                connection.writeMsg(ServerAnnounceMsg
                        .getServerAnnounceMsg(Settings.getLocalHostname() + Settings.getLocalPort()
                                , clientConnections.size(), Settings.getLocalHostname(), Settings.getLocalPort()));
            }
            try {
                Thread.sleep(Settings.getActivityInterval());
            } catch (InterruptedException e) {
                log.info("received an interrupt, system is shutting down");
                break;
            }
//            if (!term) {
//                log.debug("doing activity");
//                term = doActivity();
//            }

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
        System.out.println("listen");
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
