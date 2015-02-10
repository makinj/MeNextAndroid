package me.menext.menext;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 12/19/14.
 */
public class Submission extends ThumbnailItem{
    public String youtubeIdm;
    public String titlem;
    public int    submissionIdm;
    public int    submitterIdm;
    public String usernamem;
    public Boolean startedm;
    public int    userRatingm;
    public int    ratingm;
    public boolean canRemovem;

    public Submission(JSONObject jsonObject){
        try {
            youtubeIdm = jsonObject.getString("youtubeId");
            titlem = Html.fromHtml(jsonObject.getString("title")).toString();
            thumbnailUrlm  = Html.fromHtml(jsonObject.getString("thumbnail")).toString();
            submissionIdm = jsonObject.getInt("submissionId");
            submitterIdm = jsonObject.getInt("submitterId");
            usernamem = Html.fromHtml(jsonObject.getString("username")).toString();
            startedm = jsonObject.getInt("started")>0;
            canRemovem = jsonObject.getInt("canRemove")>0;
            userRatingm = jsonObject.getInt("userRating");
            ratingm = jsonObject.getInt("rating");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override public String getType(){
        return "Submission";
    }
}
