package layoutIP.util;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import gzf.Vec;

public class MinimumExteriorRectangle {
	public static Vec[] getMinExRect(Geometry g) {
		if (!(g instanceof Polygon)) {
			throw new IllegalArgumentException("Support polygon only!");
		}

		return getMinExRect(JTSConverter.getPolygonOuterBoundary(g.convexHull()));
	}

	public static Vec[] getMinExRect(Vec[] b) {
		int len = b.length;
		Vec[] x = new Vec[len];
		Vec[] y = new Vec[len];
		double[] minX = new double[len];
		double[] maxX = new double[len];
		double[] minY = new double[len];
		double[] maxY = new double[len];
		double[] area = new double[len];
		for (int i = 0; i < len; i++) {
			int n = (i + 1) % len;

			x[i] = b[n].dup().sub(b[i]).unit();
			y[i] = new Vec(0, 0, 1).cross(x[i]).unit();

			minX[i] = Double.POSITIVE_INFINITY;
			minY[i] = Double.POSITIVE_INFINITY;
			maxX[i] = Double.NEGATIVE_INFINITY;
			maxY[i] = Double.NEGATIVE_INFINITY;

			for (int j = 0; j < len; j++) {
				double newX, newY;

				newX = projection(b[j], x[i]);
				newY = projection(b[j], y[i]);

				minX[i] = Math.min(minX[i], newX);
				maxX[i] = Math.max(maxX[i], newX);

				minY[i] = Math.min(minY[i], newY);
				maxY[i] = Math.max(maxY[i], newY);
			}

			area[i] = (maxX[i] - minX[i]) * (maxY[i] - minY[i]);
		}

		double lastArea = area[0];
		int index = 0;
		for (int i = 0; i < len; i++) {
			if (area[i] <= lastArea) {
				index = i;
				lastArea = area[i];
			}
		}
		Vec[] newPt = new Vec[4];
		newPt[0] = x[index].dup().mul(minX[index]).add(y[index].dup().mul(minY[index]));
		newPt[1] = x[index].dup().mul(maxX[index]).add(y[index].dup().mul(minY[index]));
		newPt[2] = x[index].dup().mul(maxX[index]).add(y[index].dup().mul(maxY[index]));
		newPt[3] = x[index].dup().mul(minX[index]).add(y[index].dup().mul(maxY[index]));
		return newPt;
	}

	private static double projection(Vec pt, Vec direct) {
		Vec d;
		if (direct.len2() != 1) {
			d = direct.dup().unit();
		} else {
			d = direct;
		}
		return pt.dot(d);
	}
}
