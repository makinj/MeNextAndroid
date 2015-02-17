package me.menext.menext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class PartyFragment extends Fragment {
    MainActivity activity;
    private View view;
    private ListView submissionListView;
    private ProgressBar spinner;
    private SwipeRefreshLayout swipeLayout;
    public Party partym;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (savedInstanceState != null) {
            // Restore last state for checked position.
            partym=new Party(savedInstanceState.getInt("partyId"), savedInstanceState.getString("partyName"), savedInstanceState.getString("partyUsername"));
        }else{
            partym=new Party(-1, "Loading...", "loading...");
        }
        view = inflater.inflate(R.layout.party_fragment, container, false);

        submissionListView = (ListView)view.findViewById(R.id.party_submission_list);

        swipeLayout = (SwipeRefreshLayout) view.findViewById(R.id.party_swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh() {
                refresh();
            }
        });


        View plusButton = view.findViewById(R.id.party_plus);

        plusButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.showSearchFragment();

            }
        });


        spinner = (ProgressBar)view.findViewById(R.id.party_loading);

        submissionListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
        activity.partyThumbnailCache = new HashMap<>();
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("partyId", partym.partyIdm);
        outState.putString("partyName", partym.namem);
        outState.putString("partyUsername", partym.usernamem);
    }

    public void setParty(Party party){
        partym=party;
        activity.partyThumbnailCache.clear();
        if(view!=null){

            refresh();
        }
    }

    public void drawSubmissions(List<Submission> submissions){
        SubmissionListAdapter submissionListAdapter = new SubmissionListAdapter(submissions);
        submissionListView.setAdapter(submissionListAdapter);
        swipeLayout.setRefreshing(false);
        spinner.setVisibility(View.GONE);
        for(int i =0;i<submissions.size();i++){
            Submission submission = submissions.get(i);
            Drawable cached = activity.partyThumbnailCache.get(submission.thumbnailUrlm);
            if(cached!=null) {
                submission.thumbnailm=cached;
            }else{
                activity.setImage(submission, submissionListAdapter);
            }
        }
        submissionListAdapter.notifyDataSetChanged();
    }

    public void refresh(){
        activity.refreshParty();
        submissionListView.setAdapter(null);
        spinner.setVisibility(View.VISIBLE);


        if(partym==null){
            partym = new Party(-1, "Loading...", "Loading...");
        }
        TextView partyName = (TextView)view.findViewById(R.id.party_title);

        partyName.setText(partym.namem);

        swipeLayout.setRefreshing(false);
    }

    public class SubmissionListAdapter extends BaseAdapter {

        public List<Submission> submissionsm;
        public SubmissionListAdapter(List<Submission> submissions) {
            submissionsm = submissions;
        }
        private Drawable up_red = getResources().getDrawable( R.drawable.up_red);
        private Drawable up_gray = getResources().getDrawable( R.drawable.up_gray);
        private Drawable down_purple = getResources().getDrawable( R.drawable.down_purple);
        private Drawable down_gray = getResources().getDrawable( R.drawable.down_gray);
        private Drawable unloaded_thumbnail = getResources().getDrawable( R.drawable.unloaded_thumbnail);

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return submissionsm.size();
        }

        @Override
        public Submission getItem(int index) {
            // TODO Auto-generated method stub
            return submissionsm.get(index);
        }

        @Override
        public long getItemId(int index) {
            // TODO Auto-generated method stub
            return index;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            final ViewHolder viewHolder;
            if (view == null) {
                viewHolder=new ViewHolder();
                LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.party_submission_item, viewGroup, false);
                viewHolder.title=(TextView) view.findViewById(R.id.party_submission_item_title);
                viewHolder.thumbnail = (ImageView) view.findViewById(R.id.party_submission_item_thumbnail);
                viewHolder.upvote = (ImageView) view.findViewById(R.id.party_submission_item_upvote);
                viewHolder.downvote = (ImageView) view.findViewById(R.id.party_submission_item_downvote);
                viewHolder.remove = (ImageView) view.findViewById(R.id.party_submission_item_remove);
                viewHolder.rating = (TextView) view.findViewById(R.id.party_submission_item_rating);
                viewHolder.username = (TextView) view.findViewById(R.id.party_submission_item_username);
                viewHolder.spinner = (ProgressBar) view.findViewById(R.id.party_submission_item_loading);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }

            final Submission submission = submissionsm.get(index);

            viewHolder.title.setText(submission.titlem);
            viewHolder.username.setText(submission.usernamem);
            viewHolder.rating.setText(String.valueOf(submission.ratingm));

            if(submission.thumbnailm!=null){
                viewHolder.thumbnail.setImageDrawable(submission.thumbnailm);
            }else{
                viewHolder.thumbnail.setImageDrawable(unloaded_thumbnail);
            }

            if(submission.userRatingm>0){
                viewHolder.upvote.setImageDrawable(up_red);
                viewHolder.upvote.setOnClickListener(new VoteListener(viewHolder, submission.submissionIdm, 0));
            }else{
                viewHolder.upvote.setImageDrawable(up_gray);
                viewHolder.upvote.setOnClickListener(new VoteListener(viewHolder, submission.submissionIdm, 1));
            }
            if (submission.userRatingm<0){
                viewHolder.downvote.setImageDrawable(down_purple);
                viewHolder.downvote.setOnClickListener(new VoteListener(viewHolder, submission.submissionIdm, 0));
            }else {
                viewHolder.downvote.setImageDrawable(down_gray);
                viewHolder.downvote.setOnClickListener(new VoteListener(viewHolder, submission.submissionIdm, -1));
            }
            if (submission.canRemovem){
                viewHolder.remove.setVisibility(View.VISIBLE);
                viewHolder.remove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewHolder.spinner.setVisibility(View.VISIBLE);
                        activity.remove(submission.submissionIdm);
                    }
                });
            }else {
                viewHolder.remove.setVisibility(View.GONE);
            }
            return view;
        }

        class VoteListener implements View.OnClickListener {
            ViewHolder viewHolderm;
            int submissionIdm;
            int directionm;
            VoteListener(ViewHolder viewHolder, int submissionId, int direction){
                viewHolderm=viewHolder;
                submissionIdm=submissionId;
                directionm=direction;
            }
            @Override
            public void onClick(View view) {
                viewHolderm.spinner.setVisibility(View.VISIBLE);
                activity.vote(submissionIdm, directionm);
            }
        }

    }
    static class ViewHolder {
        TextView title;
        TextView rating;
        TextView username;
        ImageView thumbnail;
        ImageView upvote;
        ImageView downvote;
        ImageView remove;
        ProgressBar spinner;
    }
}