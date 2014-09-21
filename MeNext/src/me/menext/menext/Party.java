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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class Party extends ActionBarActivity {
	ListView queueView;
	EditText searchVideoView;
	JSONArray queueData;
	String searchTerm;
	String partyId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_party);
		Intent intent = getIntent();
		partyId = intent.getStringExtra(ListParties.PARTYID);
		
        queueView = (ListView) findViewById(R.id.queue_list);
        searchVideoView = (EditText) findViewById(R.id.search_video);
		new getQueue().execute(this);
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
		searchTerm = searchVideoView.getText().toString();
		Intent intent = new Intent(this, SearchResults.class);
		Bundle extras = new Bundle();
		extras.putString("SEARCHTERM",searchTerm);
		extras.putString("PARTYID",partyId);
		intent.putExtras(extras);
		startActivity(intent);
		return null;
	}
	
	public Void attemptGetQueue() {
		new getQueue().execute(this);
		return null;
	}
	
	public class getQueue extends AsyncTask<Activity, Void, String> {
		private Activity activity;
	    @Override
	    protected String doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "listVideos"));
	        params.add(new BasicNameValuePair("partyId", partyId));
	        return sh.makeServiceCall(activity, "menext", ServiceHandler.GET, params);
	    }

	    protected void onPostExecute(String result) {
	    	try {
				JSONObject jObject = new JSONObject(result);
				String status = jObject.getString("status");
				if (status.equalsIgnoreCase("success")) {
					ArrayList<String> videos = new ArrayList<String>();
					queueData = jObject.getJSONArray("videos");
					for(int i = 0, count = queueData.length(); i< count; i++)
					{
					    try {
					        String video = queueData.getJSONObject(i).getString("title");
					        videos.add(video);
					    }
					    catch (JSONException e) {
					        e.printStackTrace();
					    }
					}
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
			                android.R.layout.simple_list_item_1, android.R.id.text1, videos);
			        queueView.setAdapter(adapter);
				} else {
					Intent intent = new Intent(activity, LoginActivity.class);
					startActivity(intent);
					finish();
				}
	    	} catch (JSONException e) {
				e.printStackTrace();
			}
	   }
	}	
}
