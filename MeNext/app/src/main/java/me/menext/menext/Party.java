package me.menext.menext;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 12/19/14.
 */
public class Party {
    public int partyIdm;
    public String namem;
    public String usernamem;

    public Party(JSONObject jsonObject){
        try {
            partyIdm = jsonObject.getInt("partyId");
            namem = jsonObject.getString("name");
            usernamem = jsonObject.getString("username");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public Party(int partyId, String name, String username){
        partyIdm=partyId;
        namem=name;
        usernamem=username;
    }
}
