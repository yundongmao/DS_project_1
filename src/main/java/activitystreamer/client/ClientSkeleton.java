package activitystreamer.client;

import java.io.IOException;
import java.net.Socket;


import activitystreamer.messages.*;
import activitystreamer.connection.Connection;
import activitystreamer.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
//import org.json.simple.JSONObject;
//import org.json.simple.parser.JSONParser;
//import org.json.simple.parser.ParseException;

import activitystreamer.util.Settings;

public class ClientSkeleton extends Thread {
    private static final Logger log = LogManager.getLogger(ClientSkeleton.class);
    private static ClientSkeleton clientSolution;
    private TextFrame textFrame;
    private Connection con;


    public synchronized static ClientSkeleton getInstance() {
        if (clientSolution == null) {
            clientSolution = new ClientSkeleton();
        }
        return clientSolution;
    }


    public void initiateConnection() {
        // make a connection to another server if remote hostname is supplied
        if (Settings.getRemoteHostname() != null) {
            try {
                Socket socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
                System.out.println(socket);
                //TODO
                con = new Connection(socket, true);
                String msg = LoginMsg.getLoginMsg(Settings.getUsername(), Settings.getSecret());
                con.writeMsg(msg);
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                //TODO
                textFrame.setLogin();
//                System.exit(-1);
            }
        }
        textFrame.setLogin();
    }

    public ClientSkeleton() {
        System.out.println("init-------");
        textFrame = new TextFrame();
        initiateConnection();
    }


    @SuppressWarnings("unchecked")
    public void sendActivityObject(JSONObject activityObj) {
        String command = activityObj.getString("command");
        if ("REGISTER".equals(command)) {
            String username = activityObj.getString("username");
            String secret = activityObj.getString("secret");
            if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
                log.info("REGISTER fail caused by wrong params, json string: " + activityObj.toJSONString());
                textFrame.setOutputText(InvalidMsg.getInvalidMsgJSONObject("REGISTER fail caused by wrong params"));
                return;
            }
            //TODO
            boolean tempRC = con.writeMsg(RegisterMsg.getRegisterMsg(username, secret));
        } else if ("LOGIN".equals(command)) {

        } else if ("LOGOUT".equals(command)) {

        } else {
            //TODO
            System.out.println("a----------");
        }
    }

    public void logout() {
        con.writeMsg(LogoutMsg.getLogoutMsg());
        disconnect();
    }

    public void disconnect() {
        con.closeCon();
//        System.exit(0);
    }

    public void login() {
        try {
            Socket socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
            con = new Connection(socket, true);
            String msg = LoginMsg.getLoginMsg(Settings.getUsername(), Settings.getSecret());
            con.writeMsg(msg);
        } catch (IOException e) {
            log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
            //TODO
            textFrame.setLogin();
            textFrame.setOutputText("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
//                System.exit(-1);
        }
    }

    public void register(String username, String secret) {

        if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("wrong parameters", "username and secret can't be null");
            textFrame.setOutputText(jsonObject);
            return;
        }
        String msg = RegisterMsg.getRegisterMsg(username, secret);
        con.writeMsg(msg);
    }

    public void activetyMessage(String activityMsg) {
        con.writeMsg(ActivityMessageMsg.getActivityMessageMsg(Settings.getUsername()
                , Settings.getSecret(), activityMsg));

    }

    public synchronized boolean process(Connection con, String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String command = jsonObject.get("command").toString();
        if ("INVALID_MESSAGE".equals(command)) {
            return true;
        } else if ("LOGIN_SUCCESS".equals(command)) {
            log.info("LOGIN_SUCCESS :" + jsonObject.toJSONString());
            textFrame.setOutputText(jsonObject);
            textFrame.setNoLogin();
            return false;
        } else if ("REDIRECT".equals(command)) {
            textFrame.setOutputText(jsonObject);
            log.info("REDIRECT :" + jsonObject.toJSONString());
            con.closeCon();

            System.out.println("asdfasdfasfasfasdf");
            String hostname = jsonObject.getString("hostname");
            int port = jsonObject.getInteger("port");
            Settings.setRemoteHostname(hostname);
            Settings.setRemotePort(port);
            login();
            return false;
        } else if ("LOGIN_FAILED".equals(command)) {
            System.out.println("LOGIN_FAILED");
            return true;
        } else if ("ACTIVITY_BROADCAST".equals(command)) {
            log.info("ACTIVITY_BROADCAST " + jsonObject.toJSONString());
            textFrame.setOutputText(jsonObject);
            return false;
        } else if ("REGISTER_FAILED".equals(command)) {
//            System.out.println("REGISTER_FAILED");
            log.info("REGISTER_FAILED");
            textFrame.setOutputText(jsonObject);
            return false;
        } else if ("REGISTER_SUCCESS".equals(command)) {
            textFrame.setOutputText(jsonObject);
            return false;
        } else if ("AUTHTENTICATION_FAIL".equals(command)) {
            log.info("AUTHTENTICATION_FAIL for wrong username and secret ");
            return true;
        } else if ("LOCK_DENIED".equals(command)) {

        } else {
            log.info(InvalidMsg.getInvalidMsg());
            //TODO who to close the connection
            String invalidMsg = InvalidMsg.getInvalidMsg();
            con.writeMsg(invalidMsg);
            return true;
        }
        return false;
    }

    public void run() {

    }
}
