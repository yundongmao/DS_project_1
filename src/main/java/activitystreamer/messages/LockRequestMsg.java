package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class LockRequestMsg {
    private final static String command = "LOCK_REQUEST";
    public static String getLockRequestMsg(String username,String secret){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("username",username);
        jsonObject.put("secret",secret);
        return jsonObject.toJSONString();
    }
}
