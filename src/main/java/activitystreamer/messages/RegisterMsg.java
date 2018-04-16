package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class RegisterMsg {
    private final static String command = "REGISTER";
    public static String getRegisterMsg(String username,String secret){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",username);
        jsonObject.put("secret",secret);
        jsonObject.put("command",command);
        return jsonObject.toJSONString();
    }
}
