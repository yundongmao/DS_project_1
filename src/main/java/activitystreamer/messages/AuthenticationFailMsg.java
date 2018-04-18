package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class AuthenticationFailMsg {
    private final static String command = "AUTHENTICATION_FAIL";
    private String info = "";

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("info", info);
        return jsonObject;
    }

    public static JSONObject getAuthFailJSONObject(String command, String info) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("info", info);
        return jsonObject;
    }

    public static String getAuthenticationFailMsg(String info) {
        return getAuthFailJSONObject(command, info).toJSONString();
    }
}
