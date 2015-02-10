package me.menext.menext;

import android.text.Html;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 12/19/14.
 */
public class SearchResult extends ThumbnailItem {
    public static final int BLANK = 0;
    public static final int LOADING  = 1;
    public static final int SUCCESS  = 2;
    public String youtubeIdm;
    public String titlem;
    public String descriptionm;
    public int statem = BLANK;


    public SearchResult(JSONObject jsonObject){
        try {
            youtubeIdm = jsonObject.getJSONObject("id").getString("videoId");
            JSONObject snippet = jsonObject.getJSONObject("snippet");
            titlem = Html.fromHtml(snippet.getString("title")).toString();
            descriptionm = Html.fromHtml(snippet.getString("description")).toString();
            thumbnailUrlm  = Html.fromHtml(snippet.getJSONObject("thumbnails").getJSONObject("default").getString("url")).toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void setState(int state){
        statem=state;
    }

    @Override public String getType(){
        return "SearchResult";
    }
}
