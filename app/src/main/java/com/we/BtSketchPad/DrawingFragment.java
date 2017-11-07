package com.we.BtSketchPad;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Locale;

public class DrawingFragment extends Fragment {

	DrawingView drawingView;
	RelativeLayout relativeLayout;

	Float penSize = 10f;
	Float eraserSize = 20f;

	boolean isBluetoothOn = false;

	FragmentHandler fragmentHandler;
	Activity activity;
	Button[] buttons = new Button[4];
	PerspectiveTransform perspectiveTransform;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fragmentHandler = new FragmentHandler(this);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		return inflater.inflate(R.layout.content_drawing_view, container, false);
	}

	@Override
	public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		activity = getActivity();
		if (null != activity) {
			activity.setTitle("Drawing");

			drawingView = new DrawingView(activity.getApplicationContext());
			drawingView.setBackgroundColor(Color.WHITE);

			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
			params.addRule(RelativeLayout.RIGHT_OF, R.id.button_undo);

			relativeLayout = activity.findViewById(R.id.content_drawing_view);
			relativeLayout.addView(drawingView, params);

			Button buttonUndo = activity.findViewById(R.id.button_undo);
			buttonUndo.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					drawingView.recoverLastPaint();
				}
			});

			final Button buttonPen = activity.findViewById(R.id.button_pen);
			final Button buttonErase = activity.findViewById(R.id.button_eraser);
			buttonPen.setEnabled(false);
			buttonErase.setEnabled(true);

			buttonPen.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					buttonPen.setEnabled(false);
					buttonErase.setEnabled(true);
					drawingView.setPaintColor(Color.BLACK);
					drawingView.setPaintWidth(penSize);
				}
			});

			buttonErase.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					buttonPen.setEnabled(true);
					buttonErase.setEnabled(false);
					drawingView.setPaintColor(Color.WHITE);
					drawingView.setPaintWidth(eraserSize);
				}
			});

			Button buttonClear = activity.findViewById(R.id.button_clear);
			buttonClear.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					drawingView.clearView();
				}
			});

			buttons[0] = buttonUndo;
			buttons[1] = buttonPen;
			buttons[2] = buttonErase;
			buttons[3] = buttonClear;
		}
	}

	@Override
	public void onResume() {
		super.onResume();

		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
			activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else {
			int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
				uiOptions |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
					| View.SYSTEM_UI_FLAG_IMMERSIVE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			View decorView = activity.getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
			android.support.v7.app.ActionBar actionBar
				= ((SketchPadActivity) activity).getSupportActionBar();
			if (null != actionBar) {
				actionBar.hide();
			}
		}

		SharedPreferences sharedPreferences
			= activity.getApplicationContext().getSharedPreferences("Calibration", 0);
		float[] x = new float[4];
		float[] y = new float[4];
		for (int i = 0; i < 4; ++i) {
			sharedPreferences.getFloat(String.format(Locale.getDefault(), "X%d", i), x[i]);
			sharedPreferences.getFloat(String.format(Locale.getDefault(), "Y%d", i), y[i]);
		}
		Resources resources = this.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		int width = dm.widthPixels;
		int height = dm.heightPixels;
		perspectiveTransform = PerspectiveTransform.toRectangle(x, y, height, width);

		if (null != BluetoothService.getBluetoothSocket()) {
			isBluetoothOn = true;
			BluetoothService.setNewListeningThread(fragmentHandler);
		}
	}

	@Override
	public void onPause() {
		if (Build.VERSION.SDK_INT < 16) {
			activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		else {
//			View decorView = activity.getWindow().getDecorView();
//			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//				| View.SYSTEM_UI_FLAG_FULLSCREEN;
//			decorView.setSystemUiVisibility(uiOptions);
			android.support.v7.app.ActionBar actionBar
				= ((SketchPadActivity) activity).getSupportActionBar();
			if (null != actionBar) {
				View decorView = activity.getWindow().getDecorView();
				decorView.setSystemUiVisibility(0);
				actionBar.show();
			}
		}
		activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
		isBluetoothOn = false;
		BluetoothService.setNewListeningThread(null);
		super.onPause();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		relativeLayout.removeView(drawingView);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	void handleBtEvent(float x, float y) {
		float[] point = perspectiveTransform.toPoint(x, y);
		int dvLeft = drawingView.getLeft();
		if (x > dvLeft) {
			drawingView.remoteDrawerHandler(point[0] - dvLeft, point[1]);
		}
		else {
			for (Button b : buttons) {
				int bTop = b.getTop();
				int bBottom = b.getBottom();
				if (point[1] > bTop && point[1] < bBottom) {
					b.performClick();
					break;
				}
			}
		}
		TextView textView = activity.findViewById(R.id.text_text);
		String textStr
			= String.format(Locale.getDefault(), "%s%f %f\n", textView.getText(), point[0], point[1]);
		if ((-1 == point[0]) && (-1 == point[1]))
			textStr = "";
		textView.setText(textStr);
	}

	private static class FragmentHandler extends Handler {
		WeakReference<DrawingFragment> fragmentWeakReference;

		FragmentHandler(DrawingFragment fragment) {
			this.fragmentWeakReference = new WeakReference<>(fragment);
		}

		@Override
		public void handleMessage(Message msg) {
			DrawingFragment fragment = fragmentWeakReference.get();
			switch (msg.what) {
				case BluetoothService.MSG_DATA_READ:
					float[] x = (float[]) msg.obj;
					fragment.handleBtEvent(x[0], x[1]);
					break;
				case BluetoothService.MSG_LISTENING_FAILED:
					break;
				case BluetoothService.MSG_LISTENING_STANDBY:
					BluetoothService.startListeningThread();
					break;
				case BluetoothService.MSG_FINGER_LEFT:
					fragment.handleBtEvent(-1, -1);
					break;
			}
			super.handleMessage(msg);
		}
	}
}
