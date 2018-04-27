package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class ActivityMessageMsg {
    private final static String command = "ACTIVITY_MESSAGE";

    public static String getActivityMessageMsg(String username, String secret, JSONObject activity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("username", username);
        jsonObject.put("secret", secret);
        jsonObject.put("activity", activity);
        return jsonObject.toJSONString();
    }
}
