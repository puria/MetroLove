package org.sexmeter.android;

import android.app.Activity;
import android.content.Intent;

import com.googlecode.androidannotations.annotations.Click;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.Fullscreen;
import com.googlecode.androidannotations.annotations.NoTitle;

@NoTitle
@Fullscreen
@EActivity(R.layout.intro)
public class IntroActivity extends Activity {

	@Click
	void gotoTutorial() {
		startActivity(new Intent(this, TutorialActivity_.class));
	}
}
