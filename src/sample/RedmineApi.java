package sample;


import java.io.*;
import java.util.Map;

/**
 * Created by olegivancev on 11.01.15.
 */
public class RedmineApi
{
    private static RedmineApi instance;

    public String serverUrl;
    public String apiKey;

    public static RedmineApi getInstance(){
        if (instance == null){
            instance = new RedmineApi();
            instance.init();
        }
        return instance;
    }

    public void init(){
        SettingController settings = new SettingController();
        Map data = settings.getSettings();
        this.serverUrl = (String) data.get("serverUrl");
        this.apiKey = (String) data.get("apiKey");
    }

    public rdParam loadIssues(){
        rdParam param = new rdParam();
        param.url = "/issues.json";
        param.params = "assigned_to_id=me&limit=100&c[]=estimated_hours&c[]=spent_hours";
        param.method = "GET";
        return param;
    }

    public rdParam pushTime(int seconds, String issue_id, String message){
        float h = (float) seconds / 3600;
        rdParam param = new rdParam();
        param.url = "/time_entries.json";
        param.params = "issue_id="+issue_id+"&time_entry[hours]="+h+"&time_entry[activity_id]=0&time_entry[comments]="+message;
        param.method = "POST";
        return param;
    }
}
