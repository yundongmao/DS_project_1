package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class LoginFailedMsg {
    private final static String command = "LOGIN_FAILED";
    //    private final static String info = "attempt to login with wrong secret";
    private final static String info = "login failed because of no username or wrong secret";

    public static String getLoginFailedMsg() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("info", info);
        return jsonObject.toJSONString();
    }
}
