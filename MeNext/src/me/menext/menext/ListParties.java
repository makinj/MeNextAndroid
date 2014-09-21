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
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class ListParties extends ActionBarActivity {
	public static final String PARTYID = null;
	ListView joinedView;
	ListView unjoinedView;
	EditText addPartyView;
	JSONArray joinedData;
	JSONArray unjoinedData;
	String newPartyName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_parties);
        joinedView = (ListView) findViewById(R.id.joined_party_list);
        unjoinedView = (ListView) findViewById(R.id.unjoined_party_list);
        addPartyView = (EditText) findViewById(R.id.add_party);
		new getJoinedList().execute(this);
		new getUnjoinedList().execute(this);
		findViewById(R.id.add_party_button_id).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					attemptAddParty();
				}
			}
		);
		
		findViewById(R.id.log_out_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogOut();
					}
				}
			);
	}
	public Void attemptAddParty() {
		newPartyName = addPartyView.getText().toString();
		new addParty().execute(this);
		return null;
	}
	
	public Void attemptLogOut() {
		new logOut().execute(this);
		return null;
	}
	
	public Void attemptGetJoinedList(){
		new getJoinedList().execute(this);
		return null;
	}
	public Void attemptGetUnjoinedList(){
		new getUnjoinedList().execute(this);
		return null;
	}
	
	public class getJoinedList extends AsyncTask<Activity, Void, String> {
		private Activity activity;
	    @Override
	    protected String doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "listJoinedParties"));
	        return sh.makeServiceCall(activity, "menext", ServiceHandler.GET, params);
	    }

	    protected void onPostExecute(String result) {
	    	try {
				JSONObject jObject = new JSONObject(result);
				String status = jObject.getString("status");
				if (status.equalsIgnoreCase("success")) {
					ArrayList<String> parties = new ArrayList<String>();
					joinedData = jObject.getJSONArray("parties");
					for(int i = 0, count = joinedData.length(); i< count; i++)
					{
					    try {
					        String party = joinedData.getJSONObject(i).getString("name");
					        parties.add(party);
					    }
					    catch (JSONException e) {
					        e.printStackTrace();
					    }
					}
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
			                android.R.layout.simple_list_item_1, android.R.id.text1, parties);
			        joinedView.setAdapter(adapter);
	  				final JSONArray partylist = joinedData;
			        joinedView.setOnItemClickListener(new OnItemClickListener() {
		                  @Override
		                  public void onItemClick(AdapterView<?> parent, View view,
		                     int position, long id) {
		  					Intent intent = new Intent(activity, Party.class);
		  					try {
								intent.putExtra(PARTYID, partylist.getJSONObject(position).getString("partyId"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
							startActivity(intent);
		                  }
		             }); 
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
	public class getUnjoinedList extends AsyncTask<Activity, Void, String> {
		private Activity activity;
	    @Override
	    protected String doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "listUnjoinedParties"));
	        return sh.makeServiceCall(activity, "menext", ServiceHandler.GET, params);
	    }

	    protected void onPostExecute(String result) {
	    	try {
				JSONObject jObject = new JSONObject(result);
				String status = jObject.getString("status");
				if (status.equalsIgnoreCase("success")) {
					ArrayList<String> parties = new ArrayList<String>();
					unjoinedData = jObject.getJSONArray("parties");
					for(int i = 0, count = unjoinedData.length(); i< count; i++)
					{
					    try {
					        String party = unjoinedData.getJSONObject(i).getString("name");
					        parties.add(party);
					    }
					    catch (JSONException e) {
					        e.printStackTrace();
					    }
					}
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
			                android.R.layout.simple_list_item_1, android.R.id.text1, parties);
			        unjoinedView.setAdapter(adapter);
	  				final JSONArray partylist = joinedData;
			        unjoinedView.setOnItemClickListener(new OnItemClickListener() {
		                  @Override
		                  public void onItemClick(AdapterView<?> parent, View view,
		                     int position, long id) {
		  					Intent intent = new Intent(activity, Party.class);
		  					try {
								intent.putExtra(PARTYID, partylist.getJSONObject(position).getString("partyId"));
							} catch (JSONException e) {
								e.printStackTrace();
							}
							startActivity(intent);
		                  }
		             }); 
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
	
	public class logOut extends AsyncTask<Activity, Void, Void> {
		private Activity activity;
	    @Override
	    protected Void doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "logOut"));
	        sh.makeServiceCall(activity, "menext", ServiceHandler.GET, params);
	        return (Void) null;
	    }

	    protected void onPostExecute(Void arg0) {
			Intent intent = new Intent(activity, LoginActivity.class);
			startActivity(intent);
			finish();
	   }

	}
	
	public class addParty extends AsyncTask<Activity, Void, String> {
		private Activity activity;
	    @Override
	    protected String doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "createParty"));
	        params.add(new BasicNameValuePair("name", newPartyName));	        
	        return sh.makeServiceCall(activity, "menext", ServiceHandler.POST, params);
	    }

	    protected void onPostExecute(String result) {
	    	System.out.println(result);
			JSONObject jObject = null;
			try {
				jObject = new JSONObject(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String status = null;
			try {
				status = jObject.getString("status");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (status.equalsIgnoreCase("success")) {
				Intent intent = new Intent(activity, Party.class);
				try {
					intent.putExtra(PARTYID, jObject.getString("partyId"));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				startActivity(intent);
			}
	   }

	}
}
