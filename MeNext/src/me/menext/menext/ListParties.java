package me.menext.menext;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ListParties extends ActionBarActivity {
	public static final String PARTYID = null;
	ListView joinedview;
	ListView unjoinedview;
	JSONArray joinedlist;
	JSONArray unjoinedlist;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list_parties);
        joinedview = (ListView) findViewById(R.id.joined_party_list);
        unjoinedview = (ListView) findViewById(R.id.unjoined_party_list);
		new getJoinedList().execute(this);
		new getUnjoinedList().execute(this);
		findViewById(R.id.add_party_button_id).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						
					}
				});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_parties, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {

		public PlaceholderFragment() {
		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_list_parties,
					container, false);
			return rootView;
		}
	}

	public void attemptLogOut(View view) {
		new logOut().execute(this);
	}
	
	class getJoinedList extends AsyncTask<Activity, Void, String> {
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
		    	//TextView json_container = (TextView) activity.findViewById(R.id.json_container);
				JSONObject jObject = new JSONObject(result);
				String status = jObject.getString("status");
				if (status.equalsIgnoreCase("success")) {
					ArrayList<String> parties = new ArrayList<String>();
					JSONArray joinedlist = jObject.getJSONArray("parties");
					for(int i = 0, count = joinedlist.length(); i< count; i++)
					{
					    try {
					        String party = joinedlist.getJSONObject(i).getString("name");
					        parties.add(party);
					    }
					    catch (JSONException e) {
					        e.printStackTrace();
					    }
					}
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
			                android.R.layout.simple_list_item_1, android.R.id.text1, parties);
			        joinedview.setAdapter(adapter);
	  				final JSONArray partylist = joinedlist;
			        joinedview.setOnItemClickListener(new OnItemClickListener() {
		                  @Override
		                  public void onItemClick(AdapterView<?> parent, View view,
		                     int position, long id) {
		  					Intent intent = new Intent(activity, Party.class);
		  					try {
								intent.putExtra(PARTYID, partylist.getJSONObject(position).getString("partyId"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							startActivity(intent);
		                  }
		    
		             }); 
					//json_container.setText(result);
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
	
	class getUnjoinedList extends AsyncTask<Activity, Void, String> {
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
		    	//TextView json_container = (TextView) activity.findViewById(R.id.json_container);
				JSONObject jObject = new JSONObject(result);
				String status = jObject.getString("status");
				if (status.equalsIgnoreCase("success")) {
					ArrayList<String> parties = new ArrayList<String>();
					JSONArray unjoinedlist = jObject.getJSONArray("parties");
					for(int i = 0, count = unjoinedlist.length(); i< count; i++)
					{
					    try {
					        String party = unjoinedlist.getJSONObject(i).getString("name");
					        parties.add(party);
					    }
					    catch (JSONException e) {
					        e.printStackTrace();
					    }
					}
			        ArrayAdapter<String> adapter = new ArrayAdapter<String>(activity,
			                android.R.layout.simple_list_item_1, android.R.id.text1, parties);
			        unjoinedview.setAdapter(adapter);
	  				final JSONArray partylist = unjoinedlist;
			        unjoinedview.setOnItemClickListener(new OnItemClickListener() {
		                  @Override
		                  public void onItemClick(AdapterView<?> parent, View view,
		                     int position, long id) {
		  					Intent intent = new Intent(activity, Party.class);
		  					try {
								intent.putExtra(PARTYID, partylist.getJSONObject(position).getString("partyId"));
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							startActivity(intent);
		                  }
		    
		             }); 
					//json_container.setText(result);
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
	
	class logOut extends AsyncTask<Activity, Void, Void> {
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
	
	class addParty extends AsyncTask<Activity, Void, Void> {
		private Activity activity;
	    @Override
	    protected Void doInBackground(Activity... activities) {
	    	activity = activities[0];
	        ServiceHandler sh = new ServiceHandler();
	        List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("action", "createParty"));
	        params.add(new BasicNameValuePair("name", "a test party made by the app"));	        
	        sh.makeServiceCall(activity, "menext", ServiceHandler.POST, params);
	        return (Void) null;
	    }

	    protected void onPostExecute(Void arg0) {
			Intent intent = new Intent(activity, LoginActivity.class);
			startActivity(intent);
			finish();
	   }

	}
}
