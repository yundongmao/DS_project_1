package activitystreamer.messages;

import activitystreamer.util.Settings;
import com.alibaba.fastjson.JSONObject;

public class AuthenticateMsg {
    public final static String command = "AUTHENTICATE";
    private String secret = "";

    public JSONObject toJSONObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("secret", secret);
        return jsonObject;
    }

    public static JSONObject getAuthMsgJSONObject(String command, String secret) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("secret", secret);
        return jsonObject;
    }

    public static boolean auth(String secret) {
        return Settings.getSecret().equals(secret);
    }

    public static String getAuthMsgString(String secret) {
        return getAuthMsgJSONObject(command, secret).toJSONString();
    }
}
