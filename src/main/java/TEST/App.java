package TEST;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","mao");
        System.out.println(jsonObject.toJSONString());
        System.out.println("{\"name\":\"mao\"}");
        System.out.println("Hello World!");
        System.out.println(jsonObject.getString("asdfljalksdfjklasdfjklsadf"));
        Map<String,Integer> a = new HashMap<String, Integer>();
        a.put("asdfasdf",123);
        System.out.println( a.remove("asdfasdf"));
    }
}
