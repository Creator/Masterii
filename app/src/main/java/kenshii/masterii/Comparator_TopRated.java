package kenshii.masterii;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

/**
 * Created by shane- on 11/09/2015.
 */
class Comparator_TopRated implements Comparator<JSONObject>
{

    public int compare(JSONObject a, JSONObject b)
    {
        //valA and valB could be any simple type, such as number, string, whatever
        float valA = 0;
        float valB = 0;
        try {
            valA = Float.parseFloat(a.getString("bayesian_rating"));
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
        try {
            valB = Float.parseFloat(b.getString("bayesian_rating"));
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }

        if(valA < valB)
            return 1;
        if(valA > valB)
            return -1;
        return 0;
    }
}