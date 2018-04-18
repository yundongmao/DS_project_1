package activitystreamer.messages;


import com.alibaba.fastjson.JSONObject;

public class LoginSuccessMsg {
    private final static String command = "LOGIN_SUCCESS";
    private final static String preInfo = "logged in as user ";

    public static String getLoginSuccessMsg(String username) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("info", preInfo + username);
        return jsonObject.toJSONString();
    }
}
