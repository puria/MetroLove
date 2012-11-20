package org.sexmeter.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.global_fragment_layout)
public class GlobalStatisticsActivity extends Activity {
	@ViewById
	TextView italians;
	
	@ViewById
	TextView north;
	
	@ViewById
	TextView south;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getStatistics();
	}
	
	@Background
	void getStatistics() {
		RoccoAPI api = RoccoAPI.INSTANCE;
		String italians_data = api.getItalianStatistics();
		String north_data = api.getNorthItalianStatistics();
		String south_data = api.getSouthItalianStatistics();

		this.updateGlobalStatistics(italians_data, north_data, south_data);
	}

	@UiThread
	void updateGlobalStatistics(String i, String n, String s) {
		italians.setText(i);
		north.setText(n);
		south.setText(s);
	}
}
