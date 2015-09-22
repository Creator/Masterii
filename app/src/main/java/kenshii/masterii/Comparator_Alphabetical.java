package kenshii.masterii;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by shane- on 14/09/2015.
 */
class Comparator_Alphabetical implements Comparator<JSONObject> {
    @Override
    public int compare(JSONObject p1, JSONObject p2) {
        int result = 0;
        try {
            result = p1.getString("title").compareTo(p2.getString("title"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}