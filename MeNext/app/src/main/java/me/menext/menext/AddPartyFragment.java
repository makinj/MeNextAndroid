package me.menext.menext;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class AddPartyFragment extends Fragment {
    MainActivity activity;
    private View view;
    EditText partyId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        view = inflater.inflate(R.layout.add_party_fragment, container, false);

        partyId = (EditText)view.findViewById(R.id.add_party_partyId);

        Button joinButton = (Button)view.findViewById(R.id.add_party_join);

        joinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!partyId.getText().equals("")) {
                    activity.showJoinPopup(Integer.valueOf(partyId.getText().toString()));
                }
            }
        });
        Button qrButton = (Button)view.findViewById(R.id.add_party_scan);

        qrButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                activity.startQr();
            }
        });


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }
}