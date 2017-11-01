package com.we.BtSketchPad;

import android.support.annotation.NonNull;

class PerspectiveTransform {

	private float a11, a12, a13,
		a21, a22, a23,
		a31, a32, a33;

	private PerspectiveTransform(float a11, float a12, float a13,
	                             float a21, float a22, float a23,
	                             float a31, float a32, float a33) {
		this.a11 = a11;
		this.a12 = a12;
		this.a13 = a13;
		this.a21 = a21;
		this.a22 = a22;
		this.a23 = a23;
		this.a31 = a31;
		this.a32 = a32;
		this.a33 = a33;
	}

	static PerspectiveTransform toRectangle(float[] x, float[] y, int rHeight, int rWidth) {
		float dx3 = x[0] - x[1] + x[2] - x[3];
		float dy3 = y[0] - y[1] + y[2] - y[3];
		if (0.0f == dx3 && 0.0f == dy3)
			return new PerspectiveTransform(
				x[1] - x[0], x[2] - x[1], x[0],
				y[1] - y[0], y[2] - y[1], y[0],
				0.0f, 0.0f, 1.0f
			).buildJoint().scaling(rHeight, rWidth);
		float dx1 = x[1] - x[2];
		float dx2 = x[3] - x[2];
		float dy1 = y[1] - y[2];
		float dy2 = y[3] - y[2];
		float denominator = dx1 * dy2 - dx2 * dy1;
		float a13 = (dx3 * dy2 - dx2 * dy3) / denominator;
		float a23 = (dx1 * dy3 - dx3 * dy1) / denominator;
		return new PerspectiveTransform(
			x[1] - x[0] + a13 * x[1], x[3] - x[0] + a23 * x[3], x[0],
			y[1] - y[0] + a13 * y[1], y[3] - y[0] + a23 * y[3], y[0],
			a13,                      a23,                      1.0f
		).buildJoint().scaling(rHeight, rWidth);
	}

	@NonNull
	private PerspectiveTransform buildJoint() {
		return new PerspectiveTransform(
			a22 * a33 - a23 * a32, a23 * a31 - a21 * a33, a21 * a32 - a22 * a31,
			a13 * a32 - a12 * a33, a11 * a33 - a13 * a31, a12 * a31 - a11 * a32,
			a12 * a23 - a13 * a22, a13 * a21 - a11 * a23, a11 * a22 - a12 * a21
		);
	}

	@NonNull
	private PerspectiveTransform scaling(int h, int w) {
		return new PerspectiveTransform(
			w * a11, a12, a13,
			a21, h * a22, a23,
			a31, a32, a33
		);
	}

	float[] toPoint(float x, float y) {
		float denominator = a13 * x + a23 * y + a33;
		float[] result = new float[2];
		result[0] = (a11 * x + a21 * y + a31) / denominator;
		result[1] = (a12 * x + a22 * y + a32) / denominator;
		return result;
	}
}
