package org.sexmeter.android;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.googlecode.androidannotations.annotations.Background;
import com.googlecode.androidannotations.annotations.EActivity;
import com.googlecode.androidannotations.annotations.SystemService;

@EActivity(R.layout.main)
public class MetroLove extends Activity {
	private static final String TAG = "SEXMETER";
	private SharedPreferences mSettings;
	private PedometerSettings mPedometerSettings;

	private TextView mStepValueView;
	// private TextView mPaceValueView;
	private TextView mDistanceValueView;
	// private TextView mSpeedValueView;
	// private TextView mCaloriesValueView;
	TextView mDesiredPaceView;
	private int mStepValue = 0;
	private int mPaceValue = 0;
	private float mDistanceValue;
	private float mSpeedValue;
	private int mCaloriesValue;
	private float mDesiredPaceOrSpeed;
	private int mMaintain;
	private boolean mIsMetric;
	private float mMaintainInc;
	private boolean mQuitting = false;
	private boolean mIsRunning;

	@SystemService
	LocationManager locationManager;
	RoccoAPI api = RoccoAPI.INSTANCE;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		String bestProvider = locationManager.getBestProvider(criteria, true);
		Location location = locationManager.getLastKnownLocation(bestProvider);
		locationManager.requestLocationUpdates(bestProvider, 10000, 10, listener);
		api.setCurrentLocation(location);
	};

	@Override
	protected void onStart() {
		Log.i(TAG, "[ACTIVITY] onStart");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(TAG, "[ACTIVITY] onResume");
		super.onResume();

		mSettings = PreferenceManager.getDefaultSharedPreferences(this);
		mPedometerSettings = new PedometerSettings(mSettings);

		// Read from preferences if the service was running on the last onPause
		mIsRunning = mPedometerSettings.isServiceRunning();

		// Start the service if this is considered to be an application start
		// (last onPause was long ago)
		if (!mIsRunning && mPedometerSettings.isNewStart()) {
			startStepService();
			bindStepService();
		} else if (mIsRunning) {
			bindStepService();
		}

		mPedometerSettings.clearServiceRunning();

		mStepValueView = (TextView) findViewById(R.id.step_value);
		// mPaceValueView = (TextView) findViewById(R.id.pace_value);
		mDistanceValueView = (TextView) findViewById(R.id.distance_value);
		// mSpeedValueView = (TextView) findViewById(R.id.speed_value);
		// mCaloriesValueView = (TextView) findViewById(R.id.calories_value);
		// mDesiredPaceView = (TextView) findViewById(R.id.desired_pace_value);

		mIsMetric = mPedometerSettings.isMetric();
		// ((TextView) findViewById(R.id.distance_units)).setText(getString(
		// mIsMetric
		// ? R.string.kilometers
		// : R.string.miles
		// ));
		// ((TextView) findViewById(R.id.speed_units)).setText(getString(
		// mIsMetric
		// ? R.string.kilometers_per_hour
		// : R.string.miles_per_hour
		// ));

		mMaintain = mPedometerSettings.getMaintainOption();
		// ((LinearLayout)
		// this.findViewById(R.id.desired_pace_control)).setVisibility(
		// mMaintain != PedometerSettings.M_NONE
		// ? View.VISIBLE
		// : View.GONE
		// );
		if (mMaintain == PedometerSettings.M_PACE) {
			mMaintainInc = 5f;
			mDesiredPaceOrSpeed = (float) mPedometerSettings.getDesiredPace();
		} else if (mMaintain == PedometerSettings.M_SPEED) {
			mDesiredPaceOrSpeed = mPedometerSettings.getDesiredSpeed();
			mMaintainInc = 0.1f;
		}
		// Button button1 = (Button)
		// findViewById(R.id.button_desired_pace_lower);
		// button1.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// mDesiredPaceOrSpeed -= mMaintainInc;
		// mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
		// displayDesiredPaceOrSpeed();
		// setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
		// }
		// });
		// Button button2 = (Button)
		// findViewById(R.id.button_desired_pace_raise);
		// button2.setOnClickListener(new View.OnClickListener() {
		// public void onClick(View v) {
		// mDesiredPaceOrSpeed += mMaintainInc;
		// mDesiredPaceOrSpeed = Math.round(mDesiredPaceOrSpeed * 10) / 10f;
		// displayDesiredPaceOrSpeed();
		// setDesiredPaceOrSpeed(mDesiredPaceOrSpeed);
		// }
		// });
		// if (mMaintain != PedometerSettings.M_NONE) {
		// ((TextView) findViewById(R.id.desired_pace_label)).setText(
		// mMaintain == PedometerSettings.M_PACE
		// ? R.string.desired_pace
		// : R.string.desired_speed
		// );
		// }

		// displayDesiredPaceOrSpeed();
	}

	// private void displayDesiredPaceOrSpeed() {
	// if (mMaintain == PedometerSettings.M_PACE) {
	// mDesiredPaceView.setText("" + (int) mDesiredPaceOrSpeed);
	// } else {
	// mDesiredPaceView.setText("" + mDesiredPaceOrSpeed);
	// }
	// }

	@Override
	protected void onPause() {
		Log.i(TAG, "[ACTIVITY] onPause");
		if (mIsRunning) {
			unbindStepService();
		}
		if (mQuitting) {
			mPedometerSettings.saveServiceRunningWithNullTimestamp(mIsRunning);
		} else {
			mPedometerSettings.saveServiceRunningWithTimestamp(mIsRunning);
		}

		super.onPause();
		savePaceSetting();
	}

	@Override
	protected void onStop() {
		Log.i(TAG, "[ACTIVITY] onStop");
		super.onStop();
		this.stopStepService();
		String device_id = Secure.getString(getBaseContext().getContentResolver(),
				Secure.ANDROID_ID);
		this.addStatistics(mDistanceValue, device_id);
		this.resetValues(true);
		locationManager.removeUpdates(listener);
	}
	
	@Background
	void addStatistics(float mDistanceValue2, String device_id) {
		try {
			api.addStatistic(mDistanceValue2 + "", device_id);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private final LocationListener listener = new LocationListener() {
		@Override
		public void onLocationChanged(Location location) {
			api.setCurrentLocation(location);
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
			// TODO Auto-generated method stub

		}
	};

	protected void onDestroy() {
		Log.i(TAG, "[ACTIVITY] onDestroy");
		super.onDestroy();
	}

	protected void onRestart() {
		Log.i(TAG, "[ACTIVITY] onRestart");
		super.onDestroy();
	}

	private void setDesiredPaceOrSpeed(float desiredPaceOrSpeed) {
		if (mService != null) {
			if (mMaintain == PedometerSettings.M_PACE) {
				mService.setDesiredPace((int) desiredPaceOrSpeed);
			} else if (mMaintain == PedometerSettings.M_SPEED) {
				mService.setDesiredSpeed(desiredPaceOrSpeed);
			}
		}
	}

	private void savePaceSetting() {
		mPedometerSettings.savePaceOrSpeedSetting(mMaintain,
				mDesiredPaceOrSpeed);
	}

	private StepService mService;

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mService = ((StepService.StepBinder) service).getService();

			mService.registerCallback(mCallback);
			mService.reloadSettings();

		}

		public void onServiceDisconnected(ComponentName className) {
			mService = null;
		}
	};

	private void startStepService() {
		if (!mIsRunning) {
			Log.i(TAG, "[SERVICE] Start");
			mIsRunning = true;
			startService(new Intent(MetroLove.this, StepService.class));
		}
	}

	private void bindStepService() {
		Log.i(TAG, "[SERVICE] Bind");
		bindService(new Intent(MetroLove.this, StepService.class), mConnection,
				Context.BIND_AUTO_CREATE + Context.BIND_DEBUG_UNBIND);
	}

	private void unbindStepService() {
		Log.i(TAG, "[SERVICE] Unbind");
		unbindService(mConnection);
	}

	private void stopStepService() {
		Log.i(TAG, "[SERVICE] Stop");
		if (mService != null) {
			Log.i(TAG, "[SERVICE] stopService");
			stopService(new Intent(MetroLove.this, StepService.class));
		}
		mIsRunning = false;
	}

	private void resetValues(boolean updateDisplay) {
		if (mService != null && mIsRunning) {
			mService.resetValues();
		} else {
			mStepValueView.setText("0");
			// mPaceValueView.setText("0");
			mDistanceValueView.setText("0");
			// mSpeedValueView.setText("0");
			// mCaloriesValueView.setText("0");
			SharedPreferences state = getSharedPreferences("state", 0);
			SharedPreferences.Editor stateEditor = state.edit();
			if (updateDisplay) {
				stateEditor.putInt("steps", 0);
				stateEditor.putInt("pace", 0);
				stateEditor.putFloat("distance", 0);
				stateEditor.putFloat("speed", 0);
				stateEditor.putFloat("calories", 0);
				stateEditor.commit();
			}
		}
	}

	private static final int MENU_SETTINGS = 8;
	private static final int MENU_QUIT = 9;

	private static final int MENU_PAUSE = 1;
	private static final int MENU_RESUME = 2;
	private static final int MENU_RESET = 3;

	/* Creates the menu items */
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		if (mIsRunning) {
			menu.add(0, MENU_PAUSE, 0, R.string.pause)
					.setIcon(android.R.drawable.ic_media_pause)
					.setShortcut('1', 'p');
		} else {
			menu.add(0, MENU_RESUME, 0, R.string.resume)
					.setIcon(android.R.drawable.ic_media_play)
					.setShortcut('1', 'p');
		}
		menu.add(0, MENU_RESET, 0, R.string.reset)
				.setIcon(android.R.drawable.ic_menu_close_clear_cancel)
				.setShortcut('2', 'r');
		menu.add(0, MENU_SETTINGS, 0, R.string.settings)
				.setIcon(android.R.drawable.ic_menu_preferences)
				.setShortcut('8', 's')
				.setIntent(new Intent(this, Settings.class));
		menu.add(0, MENU_QUIT, 0, R.string.quit)
				.setIcon(android.R.drawable.ic_lock_power_off)
				.setShortcut('9', 'q');
		return true;
	}

	/* Handles item selections */
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_PAUSE:
			unbindStepService();
			stopStepService();
			return true;
		case MENU_RESUME:
			startStepService();
			bindStepService();
			return true;
		case MENU_RESET:
			resetValues(true);
			return true;
		case MENU_QUIT:
			resetValues(false);
			unbindStepService();
			stopStepService();
			mQuitting = true;
			finish();
			return true;
		}
		return false;
	}

	// TODO: unite all into 1 type of message
	private StepService.ICallback mCallback = new StepService.ICallback() {
		public void stepsChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(STEPS_MSG, value, 0));
		}

		public void paceChanged(int value) {
			mHandler.sendMessage(mHandler.obtainMessage(PACE_MSG, value, 0));
		}

		public void distanceChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(DISTANCE_MSG,
					(int) (value * 1000), 0));
		}

		public void speedChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(SPEED_MSG,
					(int) (value * 1000), 0));
		}

		public void caloriesChanged(float value) {
			mHandler.sendMessage(mHandler.obtainMessage(CALORIES_MSG,
					(int) (value), 0));
		}
	};

	private static final int STEPS_MSG = 1;
	private static final int PACE_MSG = 2;
	private static final int DISTANCE_MSG = 3;
	private static final int SPEED_MSG = 4;
	private static final int CALORIES_MSG = 5;

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case STEPS_MSG:
				mStepValue = (int) msg.arg1;
				mStepValueView.setText("" + mStepValue);
				break;
			case DISTANCE_MSG:
				mDistanceValue = ((int) msg.arg1) / 1000f;
				if (mDistanceValue <= 0) {
					mDistanceValueView.setText("0");
				} else {
					mDistanceValueView
							.setText(("" + (mDistanceValue + 0.000001f))
									.substring(0, 5));
				}
			default:
				super.handleMessage(msg);
			}
		}

	};

}