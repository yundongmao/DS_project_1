package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class ServerAnnounceMsg {
    private final static String command = "SERVER_ANNOUNCE";

    public static String getServerAnnounceMsg(String id, int load, String hostname, int port) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        //TODO here I use the hostname and port as id
        jsonObject.put("id", id);
        jsonObject.put("load", load);
        jsonObject.put("hostname", hostname);
        jsonObject.put("port", port);
        return jsonObject.toJSONString();
    }
}
