package com.we.BtSketchPad;

import android.content.pm.ActivityInfo;
import android.graphics.Color;
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

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		drawingView = new DrawingView(getActivity().getApplicationContext());
		drawingView.setBackgroundColor(Color.WHITE);
		fragmentHandler = new FragmentHandler(this);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater,
	                         @Nullable ViewGroup container,
	                         @Nullable Bundle savedInstanceState) {

		return inflater.inflate(R.layout.content_drawing_view, container, false);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		getActivity().setTitle("Drawing");
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
			ViewGroup.LayoutParams.MATCH_PARENT,
			ViewGroup.LayoutParams.MATCH_PARENT);
		params.addRule(RelativeLayout.RIGHT_OF, R.id.button_undo);

		relativeLayout = getActivity().findViewById(R.id.content_drawing_view);
		relativeLayout.addView(drawingView, params);

		Button buttonUndo = getActivity().findViewById(R.id.button_undo);
		buttonUndo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				drawingView.recoverLastPaint();
			}
		});

		final Button buttonPen = getActivity().findViewById(R.id.button_pen);
		final Button buttonErase = getActivity().findViewById(R.id.button_eraser);
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

		Button buttonClear = getActivity().findViewById(R.id.button_clear);
		buttonClear.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				drawingView.clearView();
			}
		});

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

		if (null != BluetoothService.getBluetoothSocket()) {
			isBluetoothOn = true;
			BluetoothService.setNewListeningThread(fragmentHandler);
		}
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
		super.onPause();
		isBluetoothOn = false;
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


	void setTextText(int x, int y) {
		TextView textView = getActivity().findViewById(R.id.text_text);
		String textStr = String.format(Locale.getDefault(), "%s%d %d\n", textView.getText(), x, y);
		if ((-1 == x) && (-1 == y))
			textStr = "";
		textView.setText(textStr);
		drawingView.remoteDrawerHandler(x, y);
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
					Integer[] x = (Integer[]) msg.obj;
					fragment.setTextText(x[0], x[1]);
					break;
				case BluetoothService.MSG_LISTENING_FAILED:
					break;
				case BluetoothService.MSG_LISTENING_STANDBY:
					BluetoothService.startListeningThread();
					break;
				case BluetoothService.MSG_FINGER_LEFT:
					fragment.setTextText(-1, -1);
					break;
			}
			super.handleMessage(msg);
		}
	}
}
