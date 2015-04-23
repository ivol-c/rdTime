package sample;

import javafx.util.Callback;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by olegivancev on 22.01.15.
 */
public class Ajax implements Runnable {


    private Callback callback;
    private RedmineApi rdApi;
    private rdParam rd;

    public Ajax(rdParam rd, Callback callback){
        rdApi = RedmineApi.getInstance();
        this.rd = rd;
        this.callback = callback;
    }

    @Override
    public void run() {
        HashMap callbackObj = new HashMap();
        callbackObj.put("rd", rd);
        try {
            Object conn = this.getConnection(rd.url, rd.params, rd.method);
            if(conn instanceof HttpURLConnection){
                String res = loadData((HttpURLConnection) conn);
                callbackObj.put("result", res);
                callbackObj.put("error", false);
                callback.call(callbackObj);
            }else {
                callbackObj.put("result", null);
                callbackObj.put("error", true);

                callback.call(callbackObj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private Object getConnection(String path, String params, String method){
        try {

            String url_str = rdApi.serverUrl + path + "?key=" + rdApi.apiKey + "&" + params;
            if(method == "POST"){
                url_str = rdApi.serverUrl + path + "?key=" + rdApi.apiKey;
            }
            URL url = new URL(url_str);

            HttpURLConnection http = (HttpURLConnection) ((url.openConnection()));

            if(method == "POST"){
                http.setRequestMethod("POST");
                http.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
                http.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(http.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();
            }else{
                http.setRequestProperty("Content-Type", "application/json");
                http.setRequestProperty("Accept", "application/json");
                http.setDoOutput(true);
                http.connect();
            }

            return http;
        }catch (Exception e){
            //e.printStackTrace();
        }
        return false;
    }

    private String loadData(HttpURLConnection conn){
        try{
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer html = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                html.append(inputLine);
            }
            in.close();
            return html.toString();
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }

}
