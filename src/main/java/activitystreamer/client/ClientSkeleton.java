package activitystreamer.client;

import java.io.IOException;
import java.net.Socket;



import activitystreamer.messages.InvalidMsg;
import activitystreamer.messages.LoginMsg;
import activitystreamer.connection.Connection;
import activitystreamer.messages.RegisterMsg;
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
        }
//        else {
//            log.error("lack of necessary parameters");
//            System.exit(-1);
//        }
    }
    public ClientSkeleton() {
        initiateConnection();
        textFrame = new TextFrame();
        start();
    }


    @SuppressWarnings("unchecked")
    public void sendActivityObject(JSONObject activityObj) {
        String command = activityObj.getString("command");
        if ("REGISTER".equals(command)){
            String username = activityObj.getString("username");
            String secret = activityObj.getString("secret");
            if(StringUtils.isNullorEmpty(username) || StringUtils.isNullorEmpty(secret)){
                log.info("REGISTER fail caused by wrong params, json string: "+activityObj.toJSONString());
                textFrame.setOutputText(InvalidMsg.getInvalidMsgJSONObject("REGISTER fail caused by wrong params"));
                return;
            }
            //TODO
            boolean tempRC = con.writeMsg(RegisterMsg.getRegisterMsg(username,secret));
        }else if ("LOGIN".equals(command)){

        }else if("LOGOUT".equals(command)){

        }else{
            //TODO
            System.out.println("a----------");
        }
    }


    public void disconnect() {

    }

    public synchronized boolean process(Connection con, String msg) {
        boolean terminate = true;
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String command = jsonObject.get("command").toString();
        if ("INVALID_MESSAGE".equals(command)){
            terminate = false;
        }else if("LOGIN_SUCCESS".equals(command)){

        }else if ("REDIRECT".equals(command)){

        }else if("LOGIN_FAILED".equals(command)){

        }else if("LOGOUT".equals(command)){

        }else if("ACTIVITY_MESSAGE".equals(command)){

        }else if("SERVER_ANNOUNCE".equals(command)){

        }else if("ACTIVITY_BROADCAST".equals(command)){

        }else if("REGISTER".equals(command)){

        }else if("REGISTER_FAILED".equals(command)){

        }else if("REGISTER_SUCCESS".equals(command)){

        }else if("LOCK_REQUEST".equals(command)){

        }else if("LOCK_DENIED".equals(command)){

        }
        else{
            //TODO who to close the connection
            String invalidMsg = InvalidMsg.getInvalidMsg();
            con.writeMsg(invalidMsg);
            terminate = true;
        }
        return terminate;
    }
    public void run() {

    }
}
