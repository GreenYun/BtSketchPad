package com.we.BtSketchPad;

import android.support.annotation.NonNull;

class PerspectiveTransform {

	private double a11, a12, a13,
		a21, a22, a23,
		a31, a32, a33;

	private PerspectiveTransform(double a11, double a12, double a13,
	                             double a21, double a22, double a23,
	                             double a31, double a32, double a33) {
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

	PerspectiveTransform toRectangle(double[][] o, double rHeight, double rWidth) {
		double[] x = o[0];
		double[] y = o[1];
		double dx3 = x[0] - x[1] + x[2] - x[3];
		double dy3 = y[0] - y[1] + y[2] - y[3];
		if (0.0f == dx3 && 0.0f == dy3)
			return new PerspectiveTransform(
				x[1] - x[0], x[2] - x[1], x[0],
				y[1] - y[0], y[2] - y[1], y[0],
				0.0f, 0.0f, 1.0f
			).buildJoint().scaling(rHeight, rWidth);
		double dx1 = x[1] - x[2];
		double dx2 = x[3] - x[2];
		double dy1 = y[1] - y[2];
		double dy2 = y[3] - y[2];
		double denominator = dx1 * dy2 - dx2 * dy1;
		double a13 = (dx3 * dy2 - dx2 * dy3) / denominator;
		double a23 = (dx1 * dy3 - dx3 * dy1) / denominator;
		return new PerspectiveTransform(
			x[1] - x[0] + a13 * x[1], x[3] - x[0] + a23 * x[3], x[0],
			y[1] - y[0] + a13 * y[1], y[3] - y[0] + a23 * y[3], y[0],
			a13,                      a23,                     1.0f
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
	private PerspectiveTransform scaling(double h, double w) {
		return new PerspectiveTransform(
			w * a11, a12, a13,
			a21, h * a22, a23,
			a31, a32, a33
		);
	}
}
