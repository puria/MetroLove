package org.sexmeter.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Fullscreen;
import com.googlecode.androidannotations.annotations.NoTitle;

@NoTitle
@Fullscreen
@EActivity(R.layout.intro)
public class IntroActivity extends Activity {
	private static final String TAG = "SexMeter";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "[ACTIVITY] onCreate");
		super.onCreate(savedInstanceState);
	}
	
	@Click
	void gotoTutorial() {
		startActivity(new Intent(this, TutorialActivity_.class));
	}
}
