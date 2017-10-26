package com.we.BtSketchPad;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import java.lang.ref.WeakReference;


public class CalibrationFragment extends Fragment {

	CalibrationView calibrationView;
	RelativeLayout relativeLayout;

	boolean isBluetoothOn = false;
	boolean isFinished = false;
	int index;
	Integer[][] calibrationData;

	FragmentHandler fragmentHandler;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		calibrationView = new CalibrationView(getActivity().getApplicationContext());
		fragmentHandler = new FragmentHandler(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {
		return inflater.inflate(R.layout.pen_calibration, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle("Calibration");
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
		relativeLayout = getActivity().findViewById(R.id.pen_calibration);
		relativeLayout.addView(calibrationView, params);
	}

	@Override
	public void onPause() {
		if (Build.VERSION.SDK_INT < 16) {
			getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
//			View decorView = getActivity().getWindow().getDecorView();
//			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//				| View.SYSTEM_UI_FLAG_FULLSCREEN;
//			decorView.setSystemUiVisibility(uiOptions);
			android.support.v7.app.ActionBar actionBar
				= ((SketchPadActivity) getActivity()).getSupportActionBar();
			if (null != actionBar) {
				View decorView = getActivity().getWindow().getDecorView();
				decorView.setSystemUiVisibility(0);
				actionBar.show();
			}
		}
		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		isBluetoothOn = false;
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_IMMERSIVE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			View decorView = getActivity().getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
			android.support.v7.app.ActionBar actionBar
				= ((SketchPadActivity) getActivity()).getSupportActionBar();
			if (null != actionBar) {
				actionBar.hide();
			}
		}
		isFinished = false;

		if (null != BluetoothService.getBluetoothSocket()) {
			isBluetoothOn = true;
			BluetoothService.setNewListeningThread(fragmentHandler);
		}
	}

	void setCalibrationData(Integer[] x) {
		calibrationData[index] = x;
		if (++index >= 5)
			isFinished = true;
		else
			calibrationView.calibrate(index);
	}

	private static class FragmentHandler extends Handler {

		WeakReference<CalibrationFragment> fragmentWeakReference;
		FragmentHandler(CalibrationFragment fragment) {
			this.fragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			CalibrationFragment fragment = fragmentWeakReference.get();
			switch (msg.what) {
				case BluetoothService.MSG_DATA_READ:
					Integer[] x = (Integer[]) msg.obj;
					fragment.setCalibrationData(x);
					break;
				case BluetoothService.MSG_LISTENING_FAILED:
					break;
				case BluetoothService.MSG_LISTENING_STANDBY:
					BluetoothService.startListeningThread();
					break;
				case BluetoothService.MSG_FINGER_LEFT:
					break;
			}
			super.handleMessage(msg);
		}

	}
}
