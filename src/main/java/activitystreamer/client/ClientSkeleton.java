package activitystreamer.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


import activitystreamer.messages.LoginMsg;
import activitystreamer.server.Connection;
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


    public static ClientSkeleton getInstance() {
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
                //TODO
                con = new Connection(socket,true);
                String msg = LoginMsg.getLoginMsg(Settings.getUsername(),Settings.getSecret());
                con.writeMsg(msg);
            } catch (IOException e) {
                log.error("failed to make connection to " + Settings.getRemoteHostname() + ":" + Settings.getRemotePort() + " :" + e);
                System.exit(-1);
            }
        }else {
            log.error("lack of necessary parameters");
            System.exit(-1);
        }
    }
    public ClientSkeleton() {
        initiateConnection();
        textFrame = new TextFrame();
        start();
    }


    @SuppressWarnings("unchecked")
    public void sendActivityObject(JSONObject activityObj) {

    }


    public void disconnect() {

    }


    public void run() {

    }
}
