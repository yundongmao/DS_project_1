package activitystreamer.messages;

import com.alibaba.fastjson.JSONObject;

public class RegisterSucessMsg {
    private final static String command = "REGISTER_SUCCESS";
    private final static String preInfo = "register success for ";

    public static String getRegisterSucessMsg(String username) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("command", command);
        jsonObject.put("infor", preInfo + username);
        return jsonObject.toJSONString();
    }
}
