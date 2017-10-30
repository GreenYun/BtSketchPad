package com.we.BtSketchPad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

public class CalibrationView extends View {

	Paint paint = new Paint();
	int pointX = 0, pointY = 0;

	public CalibrationView(Context context) {
		super(context);
		paint.setAntiAlias(true);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (pointX != 0 && pointY != 0) {
			paint.setStyle(Paint.Style.FILL);
			canvas.drawCircle(pointX, pointY, 5, paint);
			paint.setStyle(Paint.Style.STROKE);
			canvas.drawCircle(pointX, pointY, 15, paint);
		}
	}

	public void calibrate(int index) {
		int mBottom = getBottom();
		int mLeft = getLeft();
		int mRight = getRight();
		int mTop = getTop();
		switch (index) {
			case 0:
				pointX = mLeft + 50;
				pointY = mTop + 50;
				break;
			case 1:
				pointX = mLeft + 50;
				pointY = mBottom - 50;
				break;
			case 2:
				pointX = mRight - 50;
				pointY = mTop + 50;
			case 3:
				pointX = mRight - 50;
				pointY = mBottom - 50;
				break;
			case 4:
				pointX = (mRight - mLeft) / 2;
				pointY = (mBottom - mTop) / 2;
				break;
			default:
				break;
		}
		invalidate();
	}
}
