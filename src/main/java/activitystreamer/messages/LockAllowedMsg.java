package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class LockAllowedMsg {
    private final static String command = "LOCK_ALLOWED";
    public static String getLockAllowedMsg(String username,String secret){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("username",username);
        jsonObject.put("secret",secret);
        return jsonObject.toJSONString();
    }
}
