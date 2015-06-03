package sample;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import sun.tools.jstat.ParserException;

/**
 * Created by olegivancev on 12.01.15.
 */
public class Repository
{
    //private String dataFilePath = getClass().getClassLoader().getResource(".").getPath() + "data.txt";
    private String dataFilePath = new File("data.txt").getAbsolutePath();

    private Map<Object, Object> data;

    public Map getData(){
        try{
            FileInputStream fis = new FileInputStream(dataFilePath);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Map<String, String> a = (Map) ois.readObject();
            ois.close();
            fis.close();
            data = (Map) a;
            if (data instanceof Map){
                return a;
            }
        }catch(IOException ioe){
            ioe.printStackTrace();

        }catch(ClassNotFoundException c){
            System.out.println("Class not found");
            c.printStackTrace();
        }

        Map< Object, Object > map = new HashMap<Object, Object>();
        map.put("issues", new JSONArray());
        map.put("projects", new JSONArray());
        map.put("trackers", new JSONArray());
        map.put("statuses", new JSONArray());
        map.put("priorities", new JSONArray());
        map.put("active", new JSONArray());
        data = (Map) map;

        return map;

    }

    public void updateIssues(String json)
    {
        try{
            JSONObject jsonObj = (JSONObject)new JSONParser().parse(json);
            Map val = getData();

            JSONArray array = (JSONArray) jsonObj.get("issues");
            val.put("issues", array);

            JSONArray projects = new JSONArray();
            JSONArray trackers = new JSONArray();
            JSONArray statuses = new JSONArray();
            JSONArray priorities = new JSONArray();
            for(int i=0; i < array.size(); i++){
                JSONObject item = (JSONObject) array.get(i);

                JSONObject pr = (JSONObject) item.get("project");
                if(!projects.contains(pr.get("name"))) {
                    projects.add(pr.get("name"));
                }

                JSONObject tracker = (JSONObject) item.get("tracker");
                if(!trackers.contains(tracker.get("name"))) {
                    trackers.add(tracker.get("name"));
                }

                JSONObject status = (JSONObject) item.get("status");
                if(!statuses.contains(status.get("name"))) {
                    statuses.add(status.get("name"));
                }

                JSONObject priority = (JSONObject) item.get("priority");
                if(!priorities.contains(priority.get("name"))) {
                    priorities.add(priority.get("name"));
                }
            }

            val.put("projects", projects);
            val.put("trackers", trackers);
            val.put("statuses", statuses);
            val.put("priorities", priorities);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public JSONArray getIssues(){
        Map data = getData();
        return (JSONArray) data.get("issues");
    }

    public JSONArray getProjects(){
        Map data = getData();
        return (JSONArray) data.get("projects");
    }

    public JSONArray getTrackers(){
        Map data = getData();
        return (JSONArray) data.get("trackers");
    }

    public JSONArray getStatuses(){
        Map data = getData();
        return (JSONArray) data.get("statuses");
    }

    public JSONArray getPriorities(){
        Map data = getData();
        return (JSONArray) data.get("priorities");
    }

    public JSONArray getActives(){
        Map data = getData();
        return (JSONArray) data.get("active");
    }

    public JSONObject getIssue(String id){
        try{
            JSONArray data = getIssues();
            for(int i=0; i < data.size(); i++){
                JSONObject item = (JSONObject) data.get(i);

                if(id.equals(item.get("id").toString())){
                    return item;
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void addPushRequest(rdParam rd){
        Map val = getData();
        JSONArray actives = (JSONArray) val.get("active");
        actives.add(rd.params);
        val.put("active", actives);
    }

    public void removePushRequest(Object o){
        Map val = getData();
        JSONArray actives = (JSONArray) val.get("active");
        actives.remove(o);
        val.put("active", actives);
    }

    public void flush(){
        try {
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(dataFilePath));
            out.writeObject(data);
            out.flush();
            out.close();
        } catch (IOException iox) {
            iox.printStackTrace();
        }
    }
}
