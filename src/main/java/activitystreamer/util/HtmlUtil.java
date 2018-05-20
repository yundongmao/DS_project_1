package activitystreamer.util;


import activitystreamer.messages.InvalidMsg;
import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class HtmlUtil {
    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    public static String post(String url, String body){
        HttpURLConnection urlConn;
        URL mUrl = null;
        try {
            mUrl = new URL(url);
            urlConn = (HttpURLConnection) mUrl.openConnection();
            urlConn.setDoOutput(true);
            urlConn.addRequestProperty("Content-Type", "application/" + "POST");
            if (body != null) {
                urlConn.setRequestProperty("Content-Length", Integer.toString(body.length()));
                urlConn.getOutputStream().write(body.getBytes("UTF8"));
            }

            // Writing the post data to the HTTP request body
//            BufferedWriter httpRequestBodyWriter =
//                    new BufferedWriter(new OutputStreamWriter(urlConnection.getOutputStream()));
//            httpRequestBodyWriter.write("visitorName=Johnny+Jacobs&luckyNumber=1234");
//            httpRequestBodyWriter.close();

            // Reading from the HTTP response body
            Scanner httpResponseScanner = new Scanner(urlConn.getInputStream());

            if(httpResponseScanner.hasNextLine()){
                String result = httpResponseScanner.nextLine();
                httpResponseScanner.close();
                return result;
            }
            httpResponseScanner.close();
            return InvalidMsg.getInvalidMsg("no message from data service for message: "+body);

        } catch (IOException e) {
            e.printStackTrace();
            return InvalidMsg.getInvalidMsg();
        }
    }

    public static void main(String[] args) throws Exception
    {
        System.out.println(getHTML("http://localhost:8080"));
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name","mao");
        System.out.println(post("http://localhost:8080/login",jsonObject.toJSONString()));
    }
}
