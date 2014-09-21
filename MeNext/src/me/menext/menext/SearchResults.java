package me.menext.menext;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class SearchResults extends ActionBarActivity {
	ListView searchListView;
	EditText searchTermView;
	JSONArray searchData;
	String searchTerm;
	String partyId;
	String videoId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_party);
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		searchTerm = extras.getString("SEARCHTERM");
		partyId = extras.getString("PARTYID");
		
        searchListView = (ListView) findViewById(R.id.queue_list);
        searchTermView = (EditText) findViewById(R.id.search_video);
		new getResults().execute(this);
		findViewById(R.id.search_video_button_id).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					SearchVideo();
				}
			}
		);
	}
	public Void SearchVideo() {
		searchTerm = searchTermView.getText().toString();
		new getResults().execute(this);
		return null;
	}
	public Void attemptAddVideo(String vidid) {
		videoId = vidid;
		new addVideo().execute(this);
		return null;
	}	
	public class getResults extends AsyncTask<Activity, Void, String> {
		private Activity activity;
	    @Override
	    protected String doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("part", "snippet"));
	        params.add(new BasicNameValuePair("order", "relevance"));
	        params.add(new BasicNameValuePair("type", "video"));
	        params.add(new BasicNameValuePair("maxResults", "25"));
	        params.add(new BasicNameValuePair("key", "AIzaSyCfOVXmyDks2RoqmT-L54Sox1PoN-GrHsQ"));
	        params.add(new BasicNameValuePair("q", searchTerm));
	        return sh.makeServiceCall(activity, "youtube", ServiceHandler.GET, params);
	    }

	    protected void onPostExecute(String result) {
	    	try {
				JSONObject jObject = new JSONObject(result);
				searchData = jObject.getJSONArray("items");
				ArrayList<String> titles = new ArrayList<String>();
				for(int i = 0, count = searchData.length(); i< count; i++){
					try {
						String title = searchData.getJSONObject(i).getJSONObject("snippet").getString("title");
					    titles.add(title);
					}
					catch (JSONException e) {
					    e.printStackTrace();
					}
				}
			    ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
			    	android.R.layout.simple_list_item_1, android.R.id.text1, titles);
			    searchListView.setAdapter(adapter);
		        searchListView.setOnItemClickListener(new OnItemClickListener() {
	                  @Override
	                  public void onItemClick(AdapterView<?> parent, View view,
	                     int position, long id) {
	                	  try {
							String videoId = searchData.getJSONObject(position).getJSONObject("id").getString("videoId");
							attemptAddVideo(videoId);
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
	                  }
	             }); 
	    	} catch (JSONException e) {
				e.printStackTrace();
			}
	   }
	}
	public class addVideo extends AsyncTask<Activity, Void, String> {
		private Activity activity;
	    @Override
	    protected String doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "addVideo"));
	        params.add(new BasicNameValuePair("youtubeId", videoId));
	        params.add(new BasicNameValuePair("partyId", partyId));
	        return sh.makeServiceCall(activity, "menext", ServiceHandler.POST, params);
	    }

	    protected void onPostExecute(String result) {
	    	System.out.println(result);
	    }

	}
	
	
}
