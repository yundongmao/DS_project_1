package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class RegisterFailedMsg {
    private final static String command = "REGISTER_FAILED";
    private final static String endInfo = " is already registered with the system";
    public static String getRegisterFailedMsg(String username){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("info",username+endInfo);
        return jsonObject.toJSONString();
    }
}
