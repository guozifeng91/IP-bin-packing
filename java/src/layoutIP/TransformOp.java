package layoutIP;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.CoordinateSequenceFilter;
import com.vividsolutions.jts.geom.Geometry;

import gzf.Vec;
import layoutIP.util.JTSConverter;
import layoutIP.util.MinimumExteriorRectangle;

public class TransformOp {
	private static int lowerLongEdgeStarter(Vec[] rect) {
		if (rect[0].dist2(rect[1]) > rect[0].dist2(rect[3])) {
			/*
			 * starter is 0 or 2
			 */
			if (rect[0].y < rect[2].y) {
				return 0;
			} else {
				return 2;
			}
		} else {
			/*
			 * starter is 1 or 3
			 */
			if (rect[1].y < rect[3].y) {
				return 1;
			} else {
				return 3;
			}
		}
	}

	public static double align(Geometry g) {
		Vec[] outerBoundary = JTSConverter.getPolygonOuterBoundary(g);
		Vec[] minExRect = MinimumExteriorRectangle.getMinExRect(outerBoundary);
		int starter = lowerLongEdgeStarter(minExRect);

		Vec axisFrom = minExRect[(starter + 1) % 4].dup().sub(minExRect[starter]);
		Vec axisTo = Vec.xaxis;

		double rotation = axisFrom.angle(axisTo);
		double cross = axisFrom.cross(axisTo).z;
		if (cross < 0) {
			rotation *= -1;
		}

		return rotation;
	}

	/**
	 * transform geometry
	 * 
	 * @param g
	 *            geometry
	 * @param trans
	 *            translation
	 * @param center
	 *            center of rotation and flip
	 * @param r
	 *            rotation angle
	 * @param f
	 *            if flip
	 * @return
	 */
	public static Geometry transform(Geometry g, Vec trans, Vec center, double r, boolean f) {
		if (r != 0)
			g.apply(new Rotation(r, center));

		if (f)
			g.apply(new FlipX(center.x));

		g.apply(new Translation(trans));
		return g;
	}

	/**
	 * transform holes
	 * 
	 * @param g
	 *            geometry
	 * @param trans
	 *            translation
	 * @param center
	 *            center of rotation and flip
	 * @param r
	 *            rotation angle
	 * @param f
	 *            if flip
	 * @return
	 */
	public static double[][] transform(double[][] hole, Vec trans, Vec center, double r, boolean f) {
		if (hole == null)
			return null;

		int holeNum = hole.length;

		for (int i = 0; i < holeNum; i++) {
			Vec v = new Vec(hole[i][0], hole[i][1]);
			if (r != 0)
				v.rot(center, Vec.zaxis, r);

			if (f) {
				double dx = hole[i][0] - center.x;
				hole[i][0] -= 2 * dx;
			}

			v.add(trans);
			hole[i][0] = v.x;
			hole[i][1] = v.y;
		}
		return hole;
	}

	/**
	 * apply translation to a JTS geometry
	 * 
	 * @author guoguo
	 */
	static class Translation implements CoordinateSequenceFilter {
		private Vec movement;
		private boolean isDone;

		public Translation(Vec v) {
			movement = v;
			isDone = (movement.x == 0 && movement.y == 0);
		}

		public Translation(double x, double y) {
			movement = new Vec(x, y);
			isDone = (movement.x == 0 && movement.y == 0);
		}

		@Override
		public void filter(CoordinateSequence arg0, int arg1) {
			Coordinate c = arg0.getCoordinate(arg1);
			c.x = c.x + movement.x;
			c.y = c.y + movement.y;
		}

		@Override
		public boolean isDone() {
			return isDone;
		}

		@Override
		public boolean isGeometryChanged() {
			return !isDone;
		}
	}

	/**
	 * apply rotation to a JTS geometry
	 * 
	 * @author guoguo
	 *
	 */
	static class Rotation implements CoordinateSequenceFilter {
		private double rotation;
		private Vec center;

		public Rotation(double r, Vec c) {
			rotation = r;
			center = c;
		}

		@Override
		public void filter(CoordinateSequence arg0, int arg1) {
			Coordinate c = arg0.getCoordinate(arg1);
			Vec v = new Vec(c.x, c.y);
			v.rot(center, Vec.zaxis, rotation);
			// v.rot(rotation);
			c.x = v.x;
			c.y = v.y;
		}

		@Override
		public boolean isDone() {
			return rotation == 0;
		}

		@Override
		public boolean isGeometryChanged() {
			return rotation != 0;
		}
	}

	static class FlipX implements CoordinateSequenceFilter {
		double centerX;

		public FlipX(double centerX_) {
			centerX = centerX_;
		}

		@Override
		public void filter(CoordinateSequence seq, int i) {
			Coordinate c = seq.getCoordinate(i);
			double dx = c.x - centerX;
			c.x -= 2 * dx;
		}

		@Override
		public boolean isDone() {
			return false;
		}

		@Override
		public boolean isGeometryChanged() {
			return true;
		}
	}
}
