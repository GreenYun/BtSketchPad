package com.we.BtSketchPad;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;


public class DrawingView extends View {

	int i = 1;

	public DrawingView(Context context) {
		super(context);
	}

	@Override
	protected void onSizeChanged(int w, int h, int old_w, int old_h) {
		super.onSizeChanged(w, h, old_w, old_h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		++i;
//		return super.onTouchEvent(event);
		return true;
	}
}
