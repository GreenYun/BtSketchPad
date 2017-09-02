package com.we.BtSketchPad;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;

public class DrawingFragment extends Fragment {

	DrawingView drawingView;
	RelativeLayout relativeLayout;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		drawingView = new DrawingView(getActivity().getApplicationContext());
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
		relativeLayout = getActivity().findViewById(R.id.content_drawing_view);
		relativeLayout.addView(drawingView);
	}

	@Override
	public void onResume() {
		super.onResume();

		if (Build.VERSION.SDK_INT < 16) {
			getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
				| View.SYSTEM_UI_FLAG_FULLSCREEN
				| View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
			if (Build.VERSION.SDK_INT >= 19)
				uiOptions |= View.SYSTEM_UI_FLAG_IMMERSIVE
					| View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
			View decorView = getActivity().getWindow().getDecorView();
			decorView.setSystemUiVisibility(uiOptions);
			android.support.v7.app.ActionBar actionBar
				= ((SketchPadActivity) getActivity()).getSupportActionBar();
			if (null != actionBar) {
				actionBar.hide();
			}
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
}
