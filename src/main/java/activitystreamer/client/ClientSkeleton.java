package activitystreamer.client;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


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


    public static ClientSkeleton getInstance() {
        if (clientSolution == null) {
            clientSolution = new ClientSkeleton();
        }
        return clientSolution;
    }

    public ClientSkeleton() {
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
