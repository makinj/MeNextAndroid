package me.menext.menext;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.widget.ProfilePictureView;

import java.util.List;

public class JoinedFragment extends Fragment {
    MainActivity activity;
    private ListView joinedListView;
    public ProfilePictureView profilePictureView;
    private ProgressBar spinner;
    SwipeRefreshLayout swipeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.joined, container, false);

        profilePictureView = (ProfilePictureView) view.findViewById(R.id.joined_profile_pic);
        profilePictureView.setCropped(true);

        profilePictureView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.showSettingsFragment();
            }
        });

        joinedListView = (ListView) view.findViewById(R.id.joined_party_list);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.joined_swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        View plusButton = view.findViewById(R.id.joined_plus);

        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.showAddPartyFragment();
            }
        });

        spinner = (ProgressBar) view.findViewById(R.id.joined_loading);

        joinedListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if(visibleItemCount>0){
                    if (firstVisibleItem == 0&&absListView.getChildAt(0).getTop()==0){
                        swipeLayout.setEnabled(true);
                    }else{
                       swipeLayout.setEnabled(false);
                    }
                }
            }
        });

        profilePictureView.setOnTouchListener(activity.changeBackground);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activity = (MainActivity) getActivity();
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }
    public void onHiddenChanged(boolean hidden) {
        if(joinedListView!=null) {
            for (int i = 0; i < joinedListView.getChildCount(); i++) {
                joinedListView.getChildAt(i).findViewById(R.id.joined_party_item_loading).setVisibility(View.GONE);
            }
        }
    }

    public void drawParties(List<Party> parties){
        PartyListAdapter partyListAdapter = new PartyListAdapter(parties);
        joinedListView.setAdapter(partyListAdapter);
        swipeLayout.setRefreshing(false);
        spinner.setVisibility(View.GONE);
    }

    public void refresh(){
        joinedListView.setAdapter(null);
        spinner.setVisibility(View.VISIBLE);
        swipeLayout.setRefreshing(false);
        activity.refreshJoined();
    }

    public class PartyListAdapter extends BaseAdapter {

        private List<Party> partiesm;

        public PartyListAdapter(List<Party> parties) {
            partiesm=parties;

        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return partiesm.size();
        }

        @Override
        public Party getItem(int index) {
            // TODO Auto-generated method stub
            return partiesm.get(index);
        }

        @Override
        public long getItemId(int index) {
            // TODO Auto-generated method stub
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {

            if(view==null)
            {
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.joined_party_item, viewGroup,false);
            }

            TextView partyName = (TextView)view.findViewById(R.id.joined_party_item_name);
            final ProgressBar spinner = (ProgressBar)view.findViewById(R.id.joined_party_item_loading);

            final Party party = partiesm.get(index);

            partyName.setText(Html.fromHtml(party.namem).toString());

            view.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    spinner.setVisibility(View.VISIBLE);
                    activity.setParty(party);
                    //spinner.setVisibility(View.VISIBLE);
                }
            });
            return view;
        }
    }

}

