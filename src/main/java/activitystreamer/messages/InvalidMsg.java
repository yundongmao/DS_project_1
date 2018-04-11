package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class InvalidMsg {
    private final static String command = "INVALID_MESSAGE";
    private final static String info = "the received message did not contain a command";
    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("info",info);
        return jsonObject;
    }
    public static JSONObject getInvalidMsgJSONObject(String command,String info){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command",command);
        jsonObject.put("info",info);
        return jsonObject;
    }
    public static String getInvalidMsgString(){
        return getInvalidMsgJSONObject(command,info).toJSONString();
    }
}
