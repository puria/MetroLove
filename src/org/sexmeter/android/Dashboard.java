package org.sexmeter.android;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;

import android.app.Activity;
import android.content.Intent;

@EActivity(R.layout.dashboard)
public class Dashboard extends Activity {
	
	@Click
	void play() {
		startActivity(new Intent(this, MetroLove_.class));
	}

}
