package com.we.BtSketchPad;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
//import android.os.Handler;
//import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.CompoundButton;

import java.lang.ref.WeakReference;

public class SketchPadActivity extends AppCompatActivity
	implements NavigationView.OnNavigationItemSelectedListener {

	private final int REQUEST_ENABLE_BLUETOOTH = 0x1001;

//	ActivityHandler activityHandler = new ActivityHandler(this);
	ActivityBroadcastReceiver activityBroadcastReceiver
		= new ActivityBroadcastReceiver(this);

	BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//	boolean bluetoothConnectView = false;
	DrawingFragment drawingFragment = new DrawingFragment();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_sketch_pad);
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

//		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//		fab.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//					.setAction("Action", null).show();
//			}
//		});

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
			this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
		drawer.addDrawerListener(toggle);
		toggle.syncState();

		NavigationView navigationView = findViewById(R.id.nav_view);
		navigationView.setNavigationItemSelectedListener(this);

		if (null == bluetoothAdapter) {
			new AlertDialog.Builder(this)
				.setTitle("Error")
				.setMessage("Error")
				.setPositiveButton("OK", null)
				.show();
			startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
			finish();
		}
		BluetoothService.setBluetoothAdapter(bluetoothAdapter);

		registerReceiver(activityBroadcastReceiver,
			new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
		registerReceiver(activityBroadcastReceiver,
			new IntentFilter(BluetoothDevice.ACTION_FOUND));

		final SwitchCompat bluetoothSwitch
			= navigationView.getMenu()
			.findItem(R.id.nav_bluetooth)
			.getActionView()
			.findViewById(R.id.bluetooth_switch);
		bluetoothSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
				if (null != bluetoothAdapter) {
					int state = bluetoothAdapter.getState();
					if (b)
						if (BluetoothAdapter.STATE_OFF == state)
							startActivityForResult(
								new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
								REQUEST_ENABLE_BLUETOOTH);
					if (!b)
						if (BluetoothAdapter.STATE_ON == state)
							if (!bluetoothAdapter.disable())
								startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
				}
				else {
					bluetoothSwitch.setEnabled(false);
				}
			}
		});

		onBluetoothStateChanged(bluetoothAdapter.getState());
	}

	@Override
	public void onBackPressed() {
		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		if (drawer.isDrawerOpen(GravityCompat.START)) {
			drawer.closeDrawer(GravityCompat.START);
		}
		else {
			super.onBackPressed();
		}
	}

	@SuppressWarnings("StatementWithEmptyBody")
	@Override
	public boolean onNavigationItemSelected(@NonNull MenuItem item) {
		// Handle navigation view item clicks here.
		int id = item.getItemId();

//		SwitchCompat bluetoothSwitch = (SwitchCompat) findViewById(R.id.bluetooth_switch);

		FragmentTransaction fragmentTransaction;
		switch (id) {
			case R.id.nav_bluetooth:
				fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.content_main, new BluetoothSettingFragment());
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				break;
			case R.id.nav_gallery:
				fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.content_main, drawingFragment);
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				break;
			case R.id.nav_manage:
				fragmentTransaction = getSupportFragmentManager().beginTransaction();
				fragmentTransaction.replace(R.id.content_main, new CalibrationFragment());
				fragmentTransaction.addToBackStack(null);
				fragmentTransaction.commit();
				break;
			default:
				break;
		}

//		unregisterReceiver(activityBroadcastReceiver);

		DrawerLayout drawer = findViewById(R.id.drawer_layout);
		drawer.closeDrawer(GravityCompat.START);
		return true;
	}

	void onBluetoothStateChanged(int state) {
		final SwitchCompat bluetoothSwitch
			= ((NavigationView) findViewById(R.id.nav_view))
			.getMenu()
			.findItem(R.id.nav_bluetooth)
			.getActionView()
			.findViewById(R.id.bluetooth_switch);
		switch (state) {
			case BluetoothAdapter.STATE_OFF:
				BluetoothService.setBluetoothSocket(null);
				bluetoothSwitch.setEnabled(true);
				bluetoothSwitch.setChecked(false);
				break;
			case BluetoothAdapter.STATE_ON:
				bluetoothSwitch.setEnabled(true);
				bluetoothSwitch.setChecked(true);
				break;
			case BluetoothAdapter.STATE_TURNING_OFF:
			case BluetoothAdapter.STATE_TURNING_ON:
				bluetoothSwitch.setEnabled(false);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_ENABLE_BLUETOOTH:
				onBluetoothStateChanged(bluetoothAdapter.getState());
				break;
			default:
				super.onActivityResult(requestCode, resultCode, data);
				break;
		}
	}

//	private static class ActivityHandler extends Handler {
//		WeakReference<SketchPadActivity> activityWeakReference;
//
//		ActivityHandler(SketchPadActivity activity) {
//			activityWeakReference = new WeakReference<>(activity);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			SketchPadActivity activity = activityWeakReference.get();
//			switch (msg.what) {
//				default:
//					super.handleMessage(msg);
//					break;
//			}
//		}
//	}

	private static final class ActivityBroadcastReceiver extends BroadcastReceiver {
		WeakReference<SketchPadActivity> activityWeakReference;

		ActivityBroadcastReceiver(SketchPadActivity activity) {
			activityWeakReference = new WeakReference<>(activity);
		}

		@Override
		public void onReceive(Context context, Intent intent) {
			SketchPadActivity activity = activityWeakReference.get();
			String intentAction = intent.getAction();
			if (intentAction != null) {
				switch (intentAction) {
					case BluetoothAdapter.ACTION_STATE_CHANGED:
						int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
						activity.onBluetoothStateChanged(state);
						break;
					default:
						break;
				}
			}
		}
	}
}
