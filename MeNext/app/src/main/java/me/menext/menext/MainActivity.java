package me.menext.menext;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.UserSettingsFragment;

import org.apache.http.NameValuePair;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static me.menext.menext.ServiceHandler.*;


public class MainActivity extends FragmentActivity {
    //section defines constants for the fragment indexes
    private static final int JOINED = 0;//index of fragment to list of joined parties
    private static final int ADDPARTY = 1;//index of fragment to add party screen(qr join and manual party join enter)
    private static final int PARTY  = 2;//index of fragment to view and edit a party's submissions
    private static final int SEARCH  = 3;//index of fragment to search for a video and add it
    private static final int SETTINGS = 4;//index of fragment to log out of facebook
    private static final int SPLASH = 5;//index of fragment to login
    private static final int FRAGMENT_COUNT = 6;//number of fragments

    static final String STATE_OPEN_FRAGMENT = "openFragment";//index of open fragment when app instance state is saved

    private boolean authenticated=false;//variable to tell whether user is logged in with menext
    public  int userId=-1;//stores MeNext user id

    //things required for http requests
    public DefaultHttpClient httpClient;
    public SharedPreferences sharedpreferences;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];//fragment array
    private boolean isResumed;//if app is resumed
    private UiLifecycleHelper uiHelper;//facebook ui helper
    private static ServiceHandler sh;//handles http post and get requests
    View.OnTouchListener changeBackground;//listener to change the background of pressed buttons

    public HashMap<String, Drawable> partyThumbnailCache;//cache of thumbnails for the party page
    public HashMap<String, Drawable> searchThumbnailCache;//cache of thumbnails for the search page

    //when fb session changes state
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state);
        }
    };

    public MainActivity() {
        isResumed = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //for facebook login
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.main);
        FragmentManager fm = getSupportFragmentManager();//NOTE: the fragment manager seems to be the cause of a lot of errors.  Haven't researched yet

        if (savedInstanceState == null) {//create fragments for the first time
            fragments[JOINED] = new JoinedFragment();
            fragments[ADDPARTY] = new AddPartyFragment();
            fragments[PARTY] = new PartyFragment();
            fragments[SEARCH] = new SearchFragment();
            fragments[SETTINGS] = new UserSettingsFragment();
            fragments[SPLASH] = new SplashFragment();

            //add fragments and hide them
            FragmentTransaction transaction = fm.beginTransaction();
            for (Fragment fragment : fragments) {
                transaction.add(R.id.fragment_container, fragment);
                transaction.hide(fragment);
            }
            transaction.commit();
            showFragment(JOINED, false);//show joined
        }else{//retrieve fragments if instance state is saved
            fragments[SPLASH] = fm.getFragment(savedInstanceState, SplashFragment.class.getName());
            fragments[JOINED] = fm.getFragment(savedInstanceState, JoinedFragment.class.getName());
            fragments[PARTY] = fm.getFragment(savedInstanceState, PartyFragment.class.getName());
            fragments[ADDPARTY] = fm.getFragment(savedInstanceState, AddPartyFragment.class.getName());
            fragments[SETTINGS] = fm.getFragment(savedInstanceState, UserSettingsFragment.class.getName());
            fragments[SEARCH] = fm.getFragment(savedInstanceState, SearchFragment.class.getName());

            //hide all fragments that shouldn't be open
            FragmentTransaction transaction = fm.beginTransaction();
            int openFragment = savedInstanceState.getInt(STATE_OPEN_FRAGMENT);
            for (int i = 0; i < fragments.length; i++) {
                if(i!=openFragment){
                    transaction.hide(fragments[i]);
                }
            }
            transaction.commit();
        }

        httpClient = new DefaultHttpClient();
        sharedpreferences = getSharedPreferences("MeNextPreferences", Context.MODE_PRIVATE);
        String menext_domain = this.getString(R.string.menext_url);

        //setup cookies
        List<String> cookies = new ArrayList<>();
        cookies.add("PHPSESSID");
        for (int i = 0; i < cookies.size(); i++) {
            String cookie_name = cookies.get(i);
            String cookie_value = sharedpreferences.getString(cookie_name, "");
            Cookie cookie = new BasicClientCookie(cookie_name, cookie_value);
            ((BasicClientCookie) cookie).setDomain(menext_domain);
            httpClient.getCookieStore().addCookie(cookie);
        }
        sh = new ServiceHandler(this, httpClient, sharedpreferences);

        //set up background handler so buttons change color when pressed
        changeBackground = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                switch (e.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        v.setBackgroundColor(Color.argb(64, 0, 0, 0));
                        break;
                    }
                    case MotionEvent.ACTION_UP:{
                        v.setBackgroundColor(Color.TRANSPARENT);
                        break;
                    }
                }
                return false;
            }
        };

        // If the intent was started from a url then join the party corresponding to the url
        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data!=null) {
            int partyId = getPartyIdFromUri(Uri.parse(data.toString()));
            if (partyId>=0) {
                showJoinPopup(partyId);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(this);
    }

    //when qr code scanner returns this gets and handles the data that comes back
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                int partyId = getPartyIdFromUri(Uri.parse(contents));
                if (partyId>=0) {
                    showJoinPopup(partyId);
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        FragmentManager fm = getSupportFragmentManager();
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
        fm.putFragment(outState, SplashFragment.class.getName(), fragments[SPLASH]);
        fm.putFragment(outState, JoinedFragment.class.getName(), fragments[JOINED]);
        fm.putFragment(outState, PartyFragment.class.getName(), fragments[PARTY]);
        fm.putFragment(outState, SearchFragment.class.getName(), fragments[SEARCH]);
        fm.putFragment(outState, UserSettingsFragment.class.getName(), fragments[SETTINGS]);
        for (int i = 0; i < fragments.length; i++) {
            if(fragments[i].isVisible()) {
                outState.putInt(STATE_OPEN_FRAGMENT, i);
            }
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();

        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            //logged into fb, make sure auth with menext works
            checkAuth();
        } else {
            // otherwise present the splash screen and ask the user to login.
            clearBackStack();
            showFragment(SPLASH, false);
        }
    }

    //helper function for getting the partyid from a uri
    public int getPartyIdFromUri(Uri uri) {
        if (uri.getQueryParameterNames().contains("partyId")) {
            return Integer.valueOf(uri.getQueryParameter("partyId"));
        }else {
            return -1;
        }
    }

    //open qr scanning intent
    public void startQr(){
        try {
            Intent intent = new Intent("com.google.zxing.client.android.SCAN");
            intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // "PRODUCT_MODE for bar codes
            startActivityForResult(intent, 1 );

        } catch (Exception e) {

            Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
            Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
            startActivity(marketIntent);
        }
    }

    //show popup prompting user to join a party
    public void showJoinPopup(final int partyId){
        while (isFinishing());
        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) MainActivity.this.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.join_popup, viewGroup);


        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(MainActivity.this);
        popup.setContentView(layout);
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);

        findViewById(R.id.main_page_layout).post(new Runnable() {
            public void run() {
                popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });

        TextView title = (TextView)layout.findViewById(R.id.join_popup_title);
        title.setText("Are you sure you want to join Party with ID "+String.valueOf(partyId)+"?");

        final ProgressBar spinner = (ProgressBar)layout.findViewById(R.id.join_popup_loading);

        Button close = (Button)layout.findViewById(R.id.join_popup_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        Button join = (Button)layout.findViewById(R.id.join_popup_join);
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                joinParty(partyId, popup);
            }
        });

    }
    //show popup prompting user to create a party
    public void showCreatePopup(final String partyName){
        while (isFinishing());
        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) MainActivity.this.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.join_popup, viewGroup);


        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(MainActivity.this);
        popup.setContentView(layout);
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);

        findViewById(R.id.main_page_layout).post(new Runnable() {
            public void run() {
                popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });

        TextView title = (TextView)layout.findViewById(R.id.join_popup_title);
        title.setText("Are you sure you want to create a party with name "+partyName+"?");

        final ProgressBar spinner = (ProgressBar)layout.findViewById(R.id.join_popup_loading);

        Button close = (Button)layout.findViewById(R.id.join_popup_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        Button create = (Button)layout.findViewById(R.id.join_popup_join);
        create.setText("Create");
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                addParty(partyName, popup);
            }
        });

    }
    //show popup prompting user to unjoin a party
    public void showUnjoinPopup(final int partyId){
        while (isFinishing());
        // Inflate the popup_layout.xml
        RelativeLayout viewGroup = (RelativeLayout) MainActivity.this.findViewById(R.id.popup);
        LayoutInflater layoutInflater = (LayoutInflater) MainActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View layout = layoutInflater.inflate(R.layout.join_popup, viewGroup);


        // Creating the PopupWindow
        final PopupWindow popup = new PopupWindow(MainActivity.this);
        popup.setContentView(layout);
        popup.setWindowLayoutMode(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        popup.setFocusable(true);

        findViewById(R.id.main_page_layout).post(new Runnable() {
            public void run() {
                popup.showAtLocation(layout, Gravity.CENTER, 0, 0);
            }
        });

        TextView title = (TextView)layout.findViewById(R.id.join_popup_title);
        title.setText("Are you sure you want to unjoin this Party?");

        final ProgressBar spinner = (ProgressBar)layout.findViewById(R.id.join_popup_loading);

        Button close = (Button)layout.findViewById(R.id.join_popup_close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup.dismiss();
            }
        });

        Button join = (Button)layout.findViewById(R.id.join_popup_join);
        join.setText("Unjoin");
        join.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setVisibility(View.VISIBLE);
                unjoinParty(partyId,popup);
            }
        });

    }

    //executes async request for menext auth
    public void checkAuth(){
        new Authenticate().execute();
    }

    public void showAddPartyFragment() {
        showFragment(ADDPARTY, true);
    }
    public void showSettingsFragment() {
        showFragment(SETTINGS, true);
    }

    private void onSessionStateChange(Session session, SessionState state) {
        if (isResumed) {
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
            clearBackStack();
            if (state.equals(SessionState.OPENED)) {
                //logged inn with fb
                showFragment(JOINED, false);
                makeMeRequest(session);
                checkAuth();
            } else if (state.isClosed()) {
                ((JoinedFragment)fragments[JOINED]).profilePictureView.setProfileId(null);
                authenticated=false;
                userId=-1;
                showFragment(SPLASH, false);
            }
        }
    }

    //get facebook profile
    private void makeMeRequest(final Session session) {
        Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
            @Override
            public void onCompleted(GraphUser user, Response response) {
                if (session == Session.getActiveSession()) {
                    if (user != null) {
                        ((JoinedFragment)fragments[JOINED]).profilePictureView.setProfileId(user.getId());
                    }
                }
            }
        });
        request.executeAsync();
    }

    //clears stack of screens to go to when the back button is pressed
    private void clearBackStack(){
        FragmentManager manager = getSupportFragmentManager();
        int backStackSize = manager.getBackStackEntryCount();
        for (int i = 0; i < backStackSize; i++) {
            manager.popBackStack();
        }
    }

    //hides all open fragments and opens a specified fragment.  Also has option on whether to record operation in the backstack
    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else if(fragments[i].isVisible()) {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    //build and send http request for menext auth
    // NOTE: cannot be called in the ui thread (that's why we use the asynctask)
    private static String authMeNext(){
        Session session = Session.getActiveSession();
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("action", "fbLogin"));
        params.add(new BasicNameValuePair("accessToken", session.getAccessToken()));
        return sh.makeServiceCall(POST, params);
    }

    //build and send http request for checking whether you are authed with menext
    // NOTE: cannot be called in the ui thread (that's why we use the asynctask)
    private static String checkAuthMeNext(){
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("action", "loginStatus"));
        return sh.makeServiceCall(GET, params);
    }

    //build and send http request for getting the list of joined parties
    // NOTE: cannot be called in the ui thread (that's why we use the asynctask)
    private static String getJoined(){
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("action", "listJoinedParties"));
        return sh.makeServiceCall(GET, params);
    }

    //build and send http request for getting the list of submissions for a party
    // NOTE: cannot be called in the ui thread (that's why we use the asynctask)
    private static String getSubmissions(int partyId){
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("action", "listVideos"));
        params.add(new BasicNameValuePair("partyId", String.valueOf(partyId)));
        return sh.makeServiceCall(GET, params);
    }

    //build and send http request for getting the results of a youtube search
    // NOTE: cannot be called in the ui thread (that's why we use the asynctask)
    private static String getSearchResults(String query){
        List<NameValuePair> params = new ArrayList<>();
        params.add(new BasicNameValuePair("part", "snippet"));
        params.add(new BasicNameValuePair("order", "relevance"));
        params.add(new BasicNameValuePair("type", "video"));
        params.add(new BasicNameValuePair("maxResults", "25"));        params.add(new BasicNameValuePair("", ""));
        params.add(new BasicNameValuePair("q", query));
        params.add(new BasicNameValuePair("key", "AIzaSyCfOVXmyDks2RoqmT-L54Sox1PoN-GrHsQ"));

        return sh.makeServiceCall(GET, "https://www.googleapis.com/youtube/v3/search", params);
    }

    //calls asynctask to refresh the list of joined parties
    public void refreshJoined(){
        if(authenticated) {
            new GetJoinedParties().execute();
        }
    }

    //calls asynctask to refresh the list of songs in a party
    public void refreshParty(){
        if(authenticated) {
            new GetPartySubmissions().execute(((PartyFragment)fragments[PARTY]).partym.partyIdm);
        }
    }

    //calls asynctask to refresh the list of search results from youtube
    public void refreshSearch(String query){
        new GetSearchResultsTask(query).execute();
    }

    //goes to Party fragment and sets the party to be loaded
    public void setParty(Party party){
        showFragment(PARTY, true);
        ((PartyFragment)fragments[PARTY]).setParty(party);
        ((SearchFragment)fragments[SEARCH]).reset();
    }

    public void showSearchFragment(){
        showFragment(SEARCH, true);
    }

    public void setImage(ThumbnailItem itemData, BaseAdapter adapter){
        new LoadImage(itemData, adapter).execute();
    }

    public void vote(int submissionId, int direction){
        new VoteTask(submissionId, direction).execute();
    }

    public void submit(SearchResult video){
        new SubmitTask(video, ((PartyFragment)fragments[PARTY]).partym.partyIdm).execute();
    }

    public void remove(int submissionId){
        new RemoveTask(submissionId).execute();
    }

    public void CacheSubmissionThumbnail(String url, Drawable image){
        partyThumbnailCache.put(url, image);
    }

    public void CacheSearchThumbnail(String url, Drawable image){
        searchThumbnailCache.put(url, image);
    }

    public void joinParty(int partyId, PopupWindow popup){
        new JoinTask(partyId, popup).execute();
    }
    public void addParty(String partyName, PopupWindow popup){
        new AddTask(partyName, popup).execute();
    }
    public void unjoinParty(int partyId, PopupWindow popup){
        new UnjoinTask(partyId, popup).execute();
    }


    class GetJoinedParties extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void ... params) {
            return MainActivity.getJoined();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result==null){
                Log.w("MeNext", "MeNext requires an internet connection");
            }else {
                try {
                    JSONObject jResult = new JSONObject(result);
                    String status = jResult.getString("status");
                    if (status.equals("success")) {
                        JSONArray jsonParties = jResult.getJSONArray("parties");
                        List<Party> parties = new ArrayList<>();

                        for (int i = 0; i < jsonParties.length(); i++) {
                            Party party = new Party(jsonParties.getJSONObject(i));
                            parties.add(party);
                        }
                        ((JoinedFragment) fragments[JOINED]).drawParties(parties);
                    } else {
                        checkAuth();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Do anything with response..
        }
    }

    class GetPartySubmissions extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer ... params) {
            return MainActivity.getSubmissions(params[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result==null){
                Log.w("MeNext", "MeNext requires an internet connection");
            }else {
                //Log.w("MeNext", result);
                try {
                    JSONObject jResult = new JSONObject(result);
                    String status = jResult.getString("status");
                    if (status.equals("success")) {
                        JSONArray jsonSubmissions = jResult.getJSONArray("videos");
                        List<Submission> submissions = new ArrayList<>();

                        for (int i = 0; i < jsonSubmissions.length(); i++) {
                            Submission submission = new Submission(jsonSubmissions.getJSONObject(i));
                            submissions.add(submission);
                        }
                        ((PartyFragment) fragments[PARTY]).drawSubmissions(submissions);
                    } else {
                        checkAuth();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Do anything with response..
        }
    }

    class Authenticate extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void ... params) {
            MainActivity.authMeNext();
            return MainActivity.checkAuthMeNext();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if(result==null){
                Log.w("MeNext", "MeNext requires an internet connection");
            }else {
                try {
                    JSONObject jResult = new JSONObject(result);
                    Integer logged = jResult.getInt("logged");
                    //Log.w("MeNext", result);
                    if (0 == logged) {
                        clearBackStack();
                        showFragment(SPLASH, false);
                        authenticated = false;
                        userId=-1;
                    } else {
                        if (!authenticated) {
                            authenticated = true;
                            userId=jResult.getInt("userId");
                            ((JoinedFragment) fragments[JOINED]).refresh();
                            ((PartyFragment) fragments[PARTY]).refresh();

                        }
                        authenticated = true;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class LoadImage extends AsyncTask<Void, Void, Drawable> {
        public BaseAdapter adapterm;
        public ThumbnailItem itemDatam;

        public LoadImage(ThumbnailItem itemData, BaseAdapter adapter){
            adapterm=adapter;
            itemDatam=itemData;
        }

        @Override
        protected Drawable doInBackground(Void ... params) {
            try {
                InputStream is = (InputStream) new URL(itemDatam.thumbnailUrlm).getContent();
                return Drawable.createFromStream(is, "src name");
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Drawable result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                itemDatam.thumbnailm=result;
                adapterm.notifyDataSetChanged();
                if(itemDatam.getType().equals("Submission")) {
                    CacheSubmissionThumbnail(itemDatam.thumbnailUrlm, result);
                }else if (itemDatam.getType().equals("SearchResult")){
                    CacheSearchThumbnail(itemDatam.thumbnailUrlm, result);
                }
            }
        }
    }



    class SearchSuggestTask extends AsyncTask<Void, Void, String> {
        private String querym;
        public SearchSuggestTask(String query){
            querym=query;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("q", querym));
            params.add(new BasicNameValuePair("nolabels", "t"));
            params.add(new BasicNameValuePair("client", "firefox"));
            params.add(new BasicNameValuePair("ds", "yt"));
            return sh.makeServiceCall(GET, "https://suggestqueries.google.com/complete/search", params);
        }
    }

    class VoteTask extends AsyncTask<Void, Void, String> {
        private int submissionIdm;
        private int directionm;
        public VoteTask(int submissionId, int direction){
            submissionIdm=submissionId;
            directionm=direction;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "vote"));
            params.add(new BasicNameValuePair("submissionId", String.valueOf(submissionIdm)));
            params.add(new BasicNameValuePair("direction", String.valueOf(directionm)));
            return sh.makeServiceCall(POST, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                //Log.w("MeNext", result);
                refreshParty();
            }
        }
    }

    class RemoveTask extends AsyncTask<Void, Void, String> {
        private int submissionIdm;
        public RemoveTask(int submissionId){
            submissionIdm=submissionId;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "removeVideo"));
            params.add(new BasicNameValuePair("submissionId", String.valueOf(submissionIdm)));
            return sh.makeServiceCall(POST, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                //Log.w("MeNext", result);
                refreshParty();
            }
        }
    }

    class GetSearchResultsTask extends AsyncTask<Void, Void, String> {

        String querym;

        public GetSearchResultsTask(String query){
            querym=query;
        }

        @Override
        protected String doInBackground(Void ... params) {
            return MainActivity.getSearchResults(querym);
        }

        @Override
        protected void onPostExecute(String sresult) {
            super.onPostExecute(sresult);
            if(sresult==null){
                Log.w("MeNext", "MeNext requires an internet connection");
            }else {
                //Log.w("MeNext", sresult);
                try {
                    JSONObject jResult = new JSONObject(sresult);
                    JSONArray jSearchResults = jResult.getJSONArray("items");
                    List<SearchResult> results = new ArrayList<>();

                    for (int i = 0; i < jSearchResults.length(); i++) {
                        SearchResult result = new SearchResult(jSearchResults.getJSONObject(i));
                        results.add(result);
                    }
                    ((SearchFragment) fragments[SEARCH]).drawResults(results);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //Do anything with response..
        }
    }

    class SubmitTask extends AsyncTask<Void, Void, String> {
        private SearchResult videom;
        private int partyIdm;
        public SubmitTask(SearchResult video, int partyId){
            videom=video;
            partyIdm=partyId;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "addVideo"));
            params.add(new BasicNameValuePair("youtubeId", videom.youtubeIdm));
            params.add(new BasicNameValuePair("partyId", String.valueOf(partyIdm)));
            return sh.makeServiceCall(POST, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                //Log.w("MeNext", result);
                videom.setState(SearchResult.SUCCESS);
                ((SearchFragment) fragments[SEARCH]).adapter.notifyDataSetChanged();
                refreshParty();
            }
        }
    }

    class JoinTask extends AsyncTask<Void, Void, String> {
        private PopupWindow popupm;
        private int partyIdm;
        public JoinTask(int partyId, PopupWindow popup){
            popupm=popup;
            partyIdm=partyId;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "joinParty"));
            params.add(new BasicNameValuePair("partyId", String.valueOf(partyIdm)));
            return sh.makeServiceCall(POST, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                //Log.w("MeNext", result);
                popupm.dismiss();
                refreshJoined();
                clearBackStack();
                showFragment(JOINED, false);
            }
        }
    }
    class AddTask extends AsyncTask<Void, Void, String> {
        private PopupWindow popupm;
        private String partyNamem;
        public AddTask(String partyName, PopupWindow popup){
            popupm=popup;
            partyNamem=partyName;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "createParty"));
            params.add(new BasicNameValuePair("name", String.valueOf(partyNamem)));
            return sh.makeServiceCall(POST, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                //Log.w("MeNext", result);
                popupm.dismiss();
                refreshJoined();
                clearBackStack();
                showFragment(JOINED, false);
            }
        }
    }
    class UnjoinTask extends AsyncTask<Void, Void, String> {
        private PopupWindow popupm;
        private int partyIdm;
        public UnjoinTask(int partyId, PopupWindow popup){
            popupm=popup;
            partyIdm=partyId;
        }

        @Override
        protected String doInBackground(Void ... tmp) {
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("action", "unjoinParty"));
            params.add(new BasicNameValuePair("partyId", String.valueOf(partyIdm)));
            return sh.makeServiceCall(POST, params);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.w("MeNext", "MeNext requires an internet connection");
            } else {
                //Log.w("MeNext", result);
                popupm.dismiss();
                refreshJoined();
                clearBackStack();
                showFragment(JOINED, false);
            }
        }
    }

}
