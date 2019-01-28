package layoutIP.util;

import gzf.Vec;

/**
 * 
 * @author guoguo
 *
 */
public class ClockwiseManager {
	public static boolean isCounterClockwise(float[][] shape) {
		int len = shape.length;
		Vec[] shapeVec = new Vec[len];
		for (int i = 0; i < len; i++) {
			shapeVec[i] = new Vec(shape[i][0], shape[i][1], shape[i][2]);
		}
		return isCounterClockwise(shapeVec);
	}

	public static boolean isCounterClockwise(double[][] shape) {
		int len = shape.length;
		Vec[] shapeVec = new Vec[len];
		for (int i = 0; i < len; i++) {
			shapeVec[i] = new Vec(shape[i][0], shape[i][1], shape[i].length > 2 ? shape[i][2] : 0);
		}
		return isCounterClockwise(shapeVec);
	}

	public static boolean isCounterClockwise(Vec[] shape) {
		Vec crossSum = new Vec();
		int len = shape.length;

		for (int i = 0; i < len; i++) {
			int n = ((i + 1) % len);
			Vec v1 = shape[i];
			Vec v2 = shape[n];

			Vec cross = v1.cross(v2);
			crossSum.add(cross);
		}

		return crossSum.z() > 0;
	}

	public static float[][] toClockwise(float[][] shape) {
		if (!isCounterClockwise(shape)) {
			return shape;
		} else {
			int len = shape.length;
			float[][] out = new float[len][];

			for (int i = 0; i < len; i++) {
				out[i] = shape[len - i - 1];
			}

			return out;
		}
	}

	public static double[][] toClockwise(double[][] shape) {
		if (!isCounterClockwise(shape)) {
			return shape;
		} else {
			int len = shape.length;
			double[][] out = new double[len][];

			for (int i = 0; i < len; i++) {
				out[i] = shape[len - i - 1];
			}

			return out;
		}
	}

	public static Vec[] toClockwise(Vec[] shape) {
		if (!isCounterClockwise(shape)) {
			return shape;
		} else {
			int len = shape.length;
			Vec[] out = new Vec[len];

			for (int i = 0; i < len; i++) {
				out[i] = shape[len - i - 1];
			}
			return out;
		}
	}

	public static float[][] toCounterclockwise(float[][] shape) {
		if (isCounterClockwise(shape)) {
			return shape;
		} else {
			int len = shape.length;
			float[][] out = new float[len][];

			for (int i = 0; i < len; i++) {
				out[i] = shape[len - i - 1];
			}

			return out;
		}
	}

	public static double[][] toCounterclockwise(double[][] shape) {
		if (isCounterClockwise(shape)) {
			return shape;
		} else {
			int len = shape.length;
			double[][] out = new double[len][];

			for (int i = 0; i < len; i++) {
				out[i] = shape[len - i - 1];
			}

			return out;
		}
	}

	public static Vec[] toCounterclockwise(Vec[] shape) {
		if (isCounterClockwise(shape)) {
			return shape;
		} else {
			int len = shape.length;
			Vec[] out = new Vec[len];

			for (int i = 0; i < len; i++) {
				out[i] = shape[len - i - 1];
			}
			return out;
		}
	}
}
