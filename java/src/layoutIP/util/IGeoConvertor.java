package layoutIP.util;

import gzf.Vec;
import igeo.ICircle;
import igeo.ICurve;
import igeo.IG;

public class IGeoConvertor {
	private static void checkIG() {
		if (IG.cur() == null) {
			IG.init();
		}
	}

	public static ICircle[] toICircle(Vec[] center, double r, boolean containsHeight, String layer) {
		checkIG();
		ICircle[] out = new ICircle[center.length];
		for (int i = 0; i < out.length; i++) {
			out[i] = (ICircle) IG.circle(center[i].x, center[i].y, containsHeight ? center[i].z : 0, r).layer(layer);
		}
		return out;
	}

	public static ICircle toICircle(Vec center, double r, boolean containsHeight, String layer) {
		checkIG();
		return (ICircle) IG.circle(center.x, center.y, containsHeight ? center.z : 0, r).layer(layer);
	}

	public static ICircle toICircle(double[] center, double r, boolean containsHeight, String layer) {
		checkIG();
		return (ICircle) IG.circle(center[0], center[1], containsHeight ? center[2] : 0, r).layer(layer);
	}

	public static ICurve toICurve(Vec[] shape, boolean closed, boolean containHeight, String layer) {
		double[][] shape_ = new double[shape.length][];
		for (int i = 0; i < shape_.length; i++) {
			shape_[i] = new double[] { shape[i].x, shape[i].y, containHeight ? shape[i].z : 0 };
		}
		return toICurve(shape_, closed, layer);
	}

	public static ICurve toICurve(double[][] shape, boolean closed, boolean containHeight, String layer) {
		if (!containHeight) {
			double[][] shape_ = new double[shape.length][];
			for (int i = 0; i < shape_.length; i++) {
				shape_[i] = new double[] { shape[i][0], shape[i][1], 0 };
			}
			return toICurve(shape_, closed, layer);
		} else {
			return toICurve(shape, closed, layer);
		}
	}

	public static ICurve toICurve(double[][] shape, boolean closed, String layer) {
		checkIG();
		return IG.curve(shape, 1, closed).layer(layer);
	}
}
