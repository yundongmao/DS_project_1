package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class LogoutMsg {
    private final static String command = "LOGOUT";

    public static String getLogoutMsg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        return jsonObject.toJSONString();
    }

}
