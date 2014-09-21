package me.menext.menext;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class Party extends Activity {
	String partyId;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_party);
		Intent intent = getIntent();
		partyId = intent.getStringExtra(ListParties.PARTYID);
    	TextView test = (TextView) this.findViewById(R.id.test);
    	test.setText(partyId);
	}
}
