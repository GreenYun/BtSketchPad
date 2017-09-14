package com.we.BtSketchPad;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;

public class DrawingView extends View {

	Paint paint = new Paint();
//	PointF currentPoint;
	ArrayList<ColoredPath> currentPathList = new ArrayList<>();
	ArrayList<ColoredPath> pathList = new ArrayList<>();
	ColoredPath remotePath = null;
	int lastX, lastY;
	int currentColor = Color.BLACK;
	float currentWidth = 10;

	public DrawingView(Context context) {
		super(context);
		init();
	}

	private void init() {
		paint.setAntiAlias(true);
	}

	public void setPaintColor(int color) {
		currentColor = color;
	}

	public void setPaintWidth(float width) {
		currentWidth = width;
	}

	public void recoverLastPaint() {
		int size = pathList.size();
		if (size > 0)
			pathList.remove(pathList.size() - 1);
		invalidate();
	}

	public void clearView() {
		pathList.clear();
		invalidate();
	}

	@Override
	protected void onSizeChanged(int w, int h, int old_w, int old_h) {
		super.onSizeChanged(w, h, old_w, old_h);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (ColoredPath cP : pathList) {
			paint.setStyle(Paint.Style.STROKE);
			paint.setColor(cP.getColor());
			paint.setStrokeWidth(cP.getWidth());
			canvas.drawPath(cP, paint);
			paint.setStrokeWidth(0);
			paint.setStyle(Paint.Style.FILL);
			float radius = cP.getWidth() / 2;
			float[] pointStart = cP.getPointStart();
			float[] pointStop = cP.getPointStop();
			if (null != pointStart)
				canvas.drawCircle(pointStart[0], pointStart[1], radius, paint);
			if (null != pointStop)
				canvas.drawCircle(pointStop[0], pointStop[1], radius, paint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x;
		float y;
		ColoredPath cPath;
		int action = event.getActionMasked();
		int pointerIndex = event.getActionIndex();
		switch (action) {
			case MotionEvent.ACTION_POINTER_DOWN:
			case MotionEvent.ACTION_DOWN:
				x = event.getX(pointerIndex);
				y = event.getY(pointerIndex);
				cPath = new ColoredPath(currentColor, currentWidth);
				cPath.setPointStart(x, y);
				cPath.moveTo(x, y);
				currentPathList.add(pointerIndex, cPath);
				pathList.add(cPath);
				break;
			case MotionEvent.ACTION_MOVE:
				for (int i = 0; i < event.getPointerCount(); ++i) {
					float ix = event.getX(i);
					float iy = event.getY(i);
					currentPathList.get(i).lineTo(ix, iy);
				}
				break;
			case MotionEvent.ACTION_POINTER_UP:
			case MotionEvent.ACTION_UP:
				x = event.getX(pointerIndex);
				y = event.getY(pointerIndex);
				currentPathList.get(pointerIndex).setPointStop(x, y);
				currentPathList.remove(pointerIndex);
				break;
			default:
				break;
		}
		invalidate();
		return true;
	}

	void remoteDrawerHandler(int x, int y) {
		if ((-1 == x) && (-1 == y)) {
			if (null != remotePath) {
				remotePath.setPointStop(lastX, lastY);
				remotePath = null;
			}
		}
		else {
			if (null == remotePath) {
				remotePath = new ColoredPath(currentColor, currentWidth);
				remotePath.setPointStart(x, y);
				remotePath.moveTo(x, y);
				pathList.add(remotePath);
			}
			else {
				remotePath.lineTo(x, y);
				lastX = x;
				lastY = y;
			}
		}
		invalidate();
	}

	private class ColoredPath extends Path {
		float[] pointStart = null;
		float[] pointStop = null;
		int color;
		float width;

		ColoredPath(int color, float width) {
			this.color = color;
			this.width = width;
		}

		float getWidth() {
			return width;
		}

//		public void setWidth(int width) {
//			this.width = width;
//		}

		int getColor() {
			return color;
		}

//		public void setColor(int color) {
//			this.color = color;
//		}

		void setPointStart(float x, float y) {
			pointStart = new float[2];
			pointStart[0] = x;
			pointStart[1] = y;
		}

		void setPointStop(float x, float y) {
			pointStop = new float[2];
			pointStop[0] = x;
			pointStop[1] = y;
		}

		float[] getPointStart() {
			return pointStart;
		}

		float[] getPointStop() {
			return pointStop;
		}
	}

}
