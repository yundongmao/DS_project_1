package TEST;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","mao");
        System.out.println(jsonObject.toJSONString());
        System.out.println("Hello World!");
    }
}
