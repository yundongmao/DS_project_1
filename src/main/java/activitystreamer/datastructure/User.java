package activitystreamer.datastructure;

import com.alibaba.fastjson.JSONObject;

public class User {
    private String username = "";
    private String secret = "";
    public User(String _username,String _secret){
        username = _username;
        secret = _secret;
    }

    public static String getUserString(String username, String secret) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("secret", secret);
        return jsonObject.toJSONString();
    }

    public String toJSONString(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);
        jsonObject.put("secret", secret);
        return jsonObject.toJSONString();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }
}
