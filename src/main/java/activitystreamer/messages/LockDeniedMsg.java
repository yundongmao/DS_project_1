package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class LockDeniedMsg {
    private final static String command = "LOCK_DENIED";

    public static String getLockDeniedMsg(String username, String secret) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("username", username);
        jsonObject.put("secret", secret);
        return jsonObject.toJSONString();
    }
}
