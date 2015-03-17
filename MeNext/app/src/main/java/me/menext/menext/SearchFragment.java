package me.menext.menext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class SearchFragment extends Fragment {
    MainActivity activity;
    private View view;
    public ListView resultListView;
    private EditText searchBar;
    private ProgressBar spinner;
    public String querym;
    public ResultListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        if (savedInstanceState != null) {
            // Restore last state for checked position.
            querym= savedInstanceState.getString("query");
        }else{
            querym="";
        }
        view = inflater.inflate(R.layout.search_fragment, container, false);

        resultListView = (ListView)view.findViewById(R.id.search_result_list);


        searchBar = (EditText)view.findViewById(R.id.search_bar);

        View clearButton = view.findViewById(R.id.search_clear);
        clearButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                searchBar.setText("");
            }
        });

        View searchButton = view.findViewById(R.id.search_search);

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                setQuery(searchBar.getText().toString());
            }
        });

        searchBar.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    setQuery(v.getText().toString());
                }
                return false;
            }
        });

        spinner = (ProgressBar)view.findViewById(R.id.search_loading);

        searchButton.setOnTouchListener(activity.changeBackground);
        activity.searchThumbnailCache = new HashMap<>();
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
        outState.putString("query", querym);
    }

    public void setQuery(String query){
        querym=query;
        activity.searchThumbnailCache.clear();
        if(view!=null){
            refresh();
        }
    }

    public void reset(){
        searchBar.setText("");
        setQuery("");
    }

    public void drawResults(List<SearchResult> results){
        adapter = new ResultListAdapter(results);
        resultListView.setAdapter(adapter);
        spinner.setVisibility(View.GONE);
        for(int i =0;i<results.size();i++){
            SearchResult result = results.get(i);
            Drawable cached = activity.searchThumbnailCache.get(result.thumbnailUrlm);
            if(cached!=null) {
                result.thumbnailm=cached;
            }else{
                activity.setImage(result, adapter);
            }
        }
        adapter.notifyDataSetChanged();
    }

    public void refresh(){
        //resultListView.setAdapter(null);
        if(querym!=null && querym.length()!=0){
            spinner.setVisibility(View.VISIBLE);
            activity.refreshSearch(querym);
        }else{
            querym = "";
            resultListView.setAdapter(null);
        }
    }

    public class ResultListAdapter extends BaseAdapter {

        public List<SearchResult> resultsm;
        public ResultListAdapter(List<SearchResult> results) {
            resultsm = results;
        }

        private Drawable plus = getResources().getDrawable( R.drawable.plus_gray);
        private Drawable check = getResources().getDrawable( R.drawable.check_green);
        private Drawable unloaded_thumbnail = getResources().getDrawable( R.drawable.unloaded_thumbnail);


        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return resultsm.size();
        }

        @Override
        public SearchResult getItem(int index) {
            // TODO Auto-generated method stub
            return resultsm.get(index);
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
                view = inflater.inflate(R.layout.search_result_item, viewGroup, false);
                viewHolder.title=(TextView) view.findViewById(R.id.search_result_item_title);
                viewHolder.thumbnail = (ImageView) view.findViewById(R.id.search_result_item_thumbnail);
                viewHolder.submit = (ImageView) view.findViewById(R.id.search_result_item_submit);
                viewHolder.description = (TextView) view.findViewById(R.id.search_result_item_description);
                viewHolder.spinner = (ProgressBar) view.findViewById(R.id.search_result_item_loading);
                view.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) view.getTag();
            }

            final SearchResult result = resultsm.get(index);

            viewHolder.title.setText(result.titlem);
            viewHolder.description.setText(result.descriptionm);

            if(result.thumbnailm!=null){
                viewHolder.thumbnail.setImageDrawable(result.thumbnailm);
            }else{
                viewHolder.thumbnail.setImageDrawable(unloaded_thumbnail);
            }

            if(result.statem== SearchResult.BLANK){
                viewHolder.submit.setImageDrawable(plus);
                viewHolder.submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        viewHolder.spinner.setVisibility(View.VISIBLE);
                        activity.submit(result);
                    }
                });
                viewHolder.spinner.setVisibility(View.GONE);
            }else if(result.statem== SearchResult.LOADING){
                viewHolder.spinner.setVisibility(View.VISIBLE);
            }else{
                viewHolder.spinner.setVisibility(View.GONE);
                viewHolder.submit.setImageDrawable(check);
            }


            return view;
        }
    }
    static class ViewHolder {
        TextView title;
        TextView description;
        ImageView thumbnail;
        ImageView submit;
        ProgressBar spinner;
    }
}