package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class ActivityBroadcastMsg {
    private final static String command = "ACTIVITY_BROADCAST";

    public static String getActivityBroadcastMsg(JSONObject activity) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("activity", activity);
        return jsonObject.toJSONString();
    }
}
