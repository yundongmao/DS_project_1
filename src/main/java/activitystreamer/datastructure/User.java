package activitystreamer.datastructure;

import com.alibaba.fastjson.JSONObject;

public class User {
    private String username = "";
    private String secret = "";
    public static String getUserString(String username,String secret){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username",username);
        jsonObject.put("secret",secret);
        return jsonObject.toJSONString();
    }
}
