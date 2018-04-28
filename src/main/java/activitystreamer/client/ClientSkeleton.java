package activitystreamer.client;

import java.io.IOException;
import java.net.Socket;


import activitystreamer.messages.*;
import activitystreamer.connection.Connection;
import activitystreamer.util.StringUtils;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import activitystreamer.util.Settings;
import sun.jvm.hotspot.asm.Register;

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
                con = new Connection(socket, true);
                if ("anonymous".equals(Settings.getUsername())){
                    String msg = LoginMsg.getLoginMsg(Settings.getUsername(), Settings.getSecret());
                    con.writeMsg(msg);
                } else if (StringUtils.isNullorEmpty(Settings.getSecret())) {
                    String randSecret = StringUtils.getRandomString(10);
                    Settings.setSecret(randSecret);
                    String msg = RegisterMsg.getRegisterMsg(Settings.getUsername(), Settings.getSecret());
                    con.writeMsg(msg);
                    textFrame.setOutputText("auto register with randsecret : " +randSecret);
                    Thread.sleep(1000);
                    msg = LoginMsg.getLoginMsg(Settings.getUsername(), Settings.getSecret());
                    con.writeMsg(msg);
                } else {
                    String msg = LoginMsg.getLoginMsg(Settings.getUsername(), Settings.getSecret());
                    con.writeMsg(msg);
                }
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                textFrame.setLogin();
                textFrame.setRegister();
                textFrame.setOutputText("please check the host and port");
//                System.exit(-1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        textFrame.setLogin();
    }

    public ClientSkeleton() {
        textFrame = new TextFrame();
        initiateConnection();
    }

    public void logout() {
        con.writeMsg(LogoutMsg.getLogoutMsg());
        disconnect();
    }

    public void disconnect() {
        con.closeCon();
    }

    public void loginAfterSuccess(String username, String secret) {
        String msg = LoginMsg.getLoginMsg(username, secret);
        con.writeMsg(msg);
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
        Settings.setUsername(username);
        Settings.setSecret(secret);
        if (StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("wrong parameters", "username and secret can't be null");
            textFrame.setOutputText(jsonObject);
            return;
        }

        try {
            Socket socket = new Socket(Settings.getRemoteHostname(), Settings.getRemotePort());
            con = new Connection(socket, true);
            String msg = RegisterMsg.getRegisterMsg(username, secret);
            con.writeMsg(msg);
            Thread.sleep(1000);
            loginAfterSuccess(username, secret);
        } catch (IOException e) {
            log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
            //TODO
            textFrame.setLogin();
            textFrame.setOutputText("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
//                System.exit(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void activetyMessage(JSONObject activityMsg) {
        con.writeMsg(ActivityMessageMsg.getActivityMessageMsg(Settings.getUsername()
                , Settings.getSecret(), activityMsg));

    }

    public synchronized boolean process(Connection con, String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String command = jsonObject.get("command").toString();
        log.info(jsonObject);
        if ("INVALID_MESSAGE".equals(command)) {
            textFrame.setRegister();
            textFrame.setLogin();
            textFrame.setOutputText(jsonObject);
            return true;
        } else if ("LOGIN_SUCCESS".equals(command)) {
            textFrame.setOutputText(jsonObject);
            textFrame.setNoLogin();
            textFrame.setNoRegister();
            return false;
        } else if ("REDIRECT".equals(command)) {
            textFrame.setOutputText(jsonObject);
            con.closeCon();
            String hostname = jsonObject.getString("hostname");
            int port = jsonObject.getInteger("port");
            Settings.setRemoteHostname(hostname);
            Settings.setRemotePort(port);
            login();
            return false;
        } else if ("LOGIN_FAILED".equals(command)) {
            textFrame.setLogin();
            textFrame.setRegister();
            textFrame.setOutputText(jsonObject);
            return true;
        } else if ("ACTIVITY_BROADCAST".equals(command)) {
            textFrame.setOutputText(jsonObject);
            return false;
        } else if ("REGISTER_FAILED".equals(command)) {
            textFrame.setOutputText(jsonObject);
            return true;
        } else if ("REGISTER_SUCCESS".equals(command)) {
            textFrame.setOutputText(jsonObject);
            return false;
        } else if ("AUTHENTICATION_FAIL".equals(command)) {
            textFrame.setOutputText(jsonObject);
            return true;
        } else if ("SERVER_ANNOUNCE".equals(command)){
            return false;
        }else {
            textFrame.setOutputText(jsonObject);
            String invalidMsg = InvalidMsg.getInvalidMsg();
            con.writeMsg(invalidMsg);
            return true;
        }
    }

    public void run() {

    }
}
