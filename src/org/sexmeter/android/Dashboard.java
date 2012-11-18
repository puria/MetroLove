package org.sexmeter.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.UiThread;
import com.googlecode.androidannotations.annotations.ViewById;

@EActivity(R.layout.dashboard)
public class Dashboard extends Activity {
	@ViewById
	TextView personal;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getMyStatistics();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.getMyStatistics();
	}
		
	@Click
	void play() {
		startActivity(new Intent(this, MetroLove_.class));
	}
	
	@Background
	void getMyStatistics() {
		RoccoAPI api = RoccoAPI.INSTANCE;
		String device_id = Secure.getString(getBaseContext().getContentResolver(),
				Secure.ANDROID_ID);
		String total = api.getPersonalStatistics(device_id);
		updatePersonal(total);
	}
	
	@UiThread
	void updatePersonal(String total) {
		personal.setText(total);
	}

}
