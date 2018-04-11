package activitystreamer.messages;

import activitystreamer.util.Settings;
import com.alibaba.fastjson.JSONObject;

public class AuthenticationFail {
    private final static String command = "AUTHENTICATION_FAIL";
    private String info = "";
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("info",info);
        return jsonObject;
    }
    public static JSONObject getAuthFailJSONObject(String command,String info){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("info",info);
        return jsonObject;
    }
    public static String getAuthFailString(String info){
        return getAuthFailJSONObject(command,info).toJSONString();
    }
}
