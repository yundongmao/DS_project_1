package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class RedirectMsg {
    private final static String command = "REDIRECT";

    public static String getRedirectMsg(String hostname, int port) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("hostname", hostname);
        jsonObject.put("port", port);
        return jsonObject.toJSONString();
    }
}
