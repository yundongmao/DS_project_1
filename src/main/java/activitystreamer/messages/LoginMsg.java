package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class LoginMsg {
    private final static String command = "LOGIN";
    public static JSONObject getLoginMsgJSONObject(String username, String secret){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("username",username);
        jsonObject.put("secret",secret);
        return jsonObject;
    }
    public static String getLoginMsg(String username, String secret){
        return getLoginMsgJSONObject(username,secret).toJSONString();
    }
}