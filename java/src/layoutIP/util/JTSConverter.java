package layoutIP.util;

import gzf.Vec;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class JTSConverter {
	public static final GeometryFactory gf = new GeometryFactory();

	public static double[] toDoubleArray(Point p) {
		return toDoubleArray(p.getCoordinate());
	}

	public static double[] toDoubleArray(Coordinate coordinate) {
		return new double[] { coordinate.x, coordinate.y, coordinate.z };
	}

	public static float[] toFloatArray(Point p) {
		return toFloatArray(p.getCoordinate());
	}

	public static float[] toFloatArray(Coordinate coordinate) {
		return new float[] { (float) coordinate.x, (float) coordinate.y, (float) coordinate.z };
	}

	public static Vec toVec(Point p) {
		return toVec(p.getCoordinate());
	}

	public static Vec toVec(Coordinate coordinate) {
		return new Vec(coordinate.x, coordinate.y, Double.isNaN(coordinate.z) ? 0 : coordinate.z);
	}

	public static Coordinate toCoordinate(double[] c) {
		return new Coordinate(c[0], c[1], c.length > 2 ? c[2] : 0);
	}

	public static Coordinate toCoordinate(float[] c) {
		return new Coordinate(c[0], c[1], c.length > 2 ? c[2] : 0);
	}

	public static Coordinate toCoordinate(Vec v) {
		return new Coordinate(v.x, v.y, v.z);
	}

	public static Point toPoint(double[] c) {
		return gf.createPoint(toCoordinate(c));
	}

	public static Point toPoint(float[] c) {
		return gf.createPoint(toCoordinate(c));
	}

	public static Point toPoint(Vec v) {
		return gf.createPoint(toCoordinate(v));
	}

	/**
	 * convert an array of float into polygon
	 * 
	 * @param shape
	 * @param containsHeight
	 * @return
	 */
	public static Polygon toPolygon(float[][] shape, boolean containsHeight) {
		int vertexNum = shape.length;
		Coordinate[] coord = new Coordinate[vertexNum + 1];
		for (int i = 0; i < vertexNum; i++) {
			coord[i] = new Coordinate(shape[i][0], shape[i][1], containsHeight ? shape[i][2] : 0);
		}
		coord[vertexNum] = coord[0];
		return gf.createPolygon(coord);
	}

	/**
	 * convert an array of double into polygon
	 * 
	 * @param shape
	 * @param containsHeight
	 * @return
	 */
	public static Polygon toPolygon(double[][] shape, boolean containsHeight) {
		int vertexNum = shape.length;
		Coordinate[] coord = new Coordinate[vertexNum + 1];
		for (int i = 0; i < vertexNum; i++) {
			coord[i] = new Coordinate(shape[i][0], shape[i][1], containsHeight ? shape[i][2] : 0);
		}
		coord[vertexNum] = coord[0];
		return gf.createPolygon(coord);
	}

	/**
	 * convert an array of Vec into polygon
	 * 
	 * @param shape
	 * @param containsHeight
	 * @return
	 */
	public static Polygon toPolygon(Vec[] shape, boolean containsHeight) {
		// int vertexNum = shape.length;
		// Coordinate[] coord = new Coordinate[vertexNum + 1];
		// for (int i = 0; i < vertexNum; i++) {
		// coord[i] = new Coordinate(shape[i].x, shape[i].y, containsHeight ?
		// shape[i].z : 0);
		// }
		// coord[vertexNum] = coord[0];
		return gf.createPolygon(toLinearRing(shape, containsHeight));
	}

	/**
	 * convert an array of Vec into polygon
	 * 
	 * @param shape
	 * @param innerRing
	 * @param containsHeight
	 * @return
	 */
	public static Polygon toPolygon(Vec[] shape, Vec[][] innerRing, boolean containsHeight) {
		// int vertexNum = shape.length;
		// Coordinate[] coord = new Coordinate[vertexNum + 1];
		// for (int i = 0; i < vertexNum; i++) {
		// coord[i] = new Coordinate(shape[i].x, shape[i].y, containsHeight ?
		// shape[i].z : 0);
		// }
		// coord[vertexNum] = coord[0];

		LinearRing shell = toLinearRing(shape, containsHeight);

		LinearRing[] hole = new LinearRing[innerRing.length];
		for (int i = 0; i < hole.length; i++) {
			hole[i] = toLinearRing(innerRing[i], containsHeight);
		}

		if (hole.length > 0) {
			return gf.createPolygon(shell, hole);
		} else {
			return gf.createPolygon(shell);
		}

	}

	/**
	 * 
	 * @param shape
	 * @return
	 */
	public static LinearRing toLinearRing(Vec[] shape, boolean containsHeight) {
		if (shape[0].dist2(shape[shape.length - 1]) == 0) {
			return gf.createLinearRing(toCoordinateKeepOrigin(shape, containsHeight));
		} else {
			return gf.createLinearRing(toCoordinateAddTail(shape, containsHeight));
		}
	}

	/**
	 * The first and last coordinates are the same object
	 * 
	 * @param shape
	 * @param containsHeight
	 * @return
	 */
	protected static Coordinate[] toCoordinateAddTail(Vec[] shape, boolean containsHeight) {
		int vertexNum = shape.length;
		Coordinate[] coord = new Coordinate[vertexNum + 1];
		for (int i = 0; i < vertexNum; i++) {
			coord[i] = new Coordinate(shape[i].x, shape[i].y, containsHeight ? shape[i].z : 0);
		}
		coord[vertexNum] = coord[0];

		return coord;
	}

	/**
	 * 
	 * @param shape
	 * @param containsHeight
	 * @return
	 */
	protected static Coordinate[] toCoordinateKeepOrigin(Vec[] shape, boolean containsHeight) {
		int vertexNum = shape.length;
		Coordinate[] coord = new Coordinate[vertexNum];
		for (int i = 0; i < vertexNum; i++) {
			coord[i] = new Coordinate(shape[i].x, shape[i].y, containsHeight ? shape[i].z : 0);
		}

		return coord;
	}

	/**
	 * check type of geometry first, the last coordinate will be discarded
	 * 
	 * @param g
	 * @return
	 */
	protected static Vec[] toVecArray(Geometry g) {
		Coordinate[] coord = g.getCoordinates();
		int len = coord.length - 1;
		Vec[] out = new Vec[len];
		for (int i = 0; i < len; i++) {
			out[i] = new Vec(coord[i].x, coord[i].y, 0);
		}
		return out;
	}

	/**
	 * check type of geometry first, all coordinate will remain
	 * 
	 * @param g
	 * @return
	 */
	protected static Vec[] toVecArrayKeepAll(Geometry g) {
		Coordinate[] coord = g.getCoordinates();
		int len = coord.length;
		Vec[] out = new Vec[len];
		for (int i = 0; i < len; i++) {
			out[i] = new Vec(coord[i].x, coord[i].y, 0);
		}
		return out;
	}

	/**
	 * 
	 * @param g
	 * @return
	 */
	public static Vec[] getPolygonOuterBoundary(Geometry g) {
		if (!(g instanceof Polygon)) {
			// System.out.println("Not polygon!");
			return null;
		}

		Polygon p = (Polygon) g;
		Vec[] out = toVecArray(p.getExteriorRing());

		return ClockwiseManager.toCounterclockwise(out);
	}

	/**
	 * 
	 * @param g
	 * @return
	 */
	public static Vec[][] getPolygonInnerBoundary(Geometry g) {
		if (!(g instanceof Polygon)) {
			return null;
		}

		Polygon p = (Polygon) g;
		int num = p.getNumInteriorRing();
		Vec[][] out = new Vec[num][];
		for (int i = 0; i < num; i++) {
			out[i] = toVecArray(p.getInteriorRingN(i));
			out[i] = ClockwiseManager.toClockwise(out[i]);
		}
		return out;
	}
}
