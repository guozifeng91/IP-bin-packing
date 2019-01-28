package layoutIP.util;

import gzf.Vec;

/**
 * calculate curvature of a polygon
 * 
 * @author guoguo
 *
 */
public class CurvatureCalculator {
	public static Vec[] toVecArray(float[][] shape) {
		int len = shape.length;
		Vec[] shapeVec = new Vec[len];
		for (int i = 0; i < len; i++) {
			shapeVec[i] = new Vec(shape[i][0], shape[i][1], shape[i][2]);
		}
		return shapeVec;
	}

	public static Vec[] toVecArray(double[][] shape) {
		int len = shape.length;
		Vec[] shapeVec = new Vec[len];
		for (int i = 0; i < len; i++) {
			shapeVec[i] = new Vec(shape[i][0], shape[i][1], shape[i][2]);
		}
		return shapeVec;
	}

	public static float[][] toFloatArray(Vec[] pt) {
		int ptSize = pt.length;
		float[][] ptF = new float[ptSize][3];
		for (int i = 0; i < ptSize; i++) {
			ptF[i][0] = (float) pt[i].x;
			ptF[i][1] = (float) pt[i].y;
			ptF[i][2] = (float) pt[i].z;

		}

		return ptF;
	}

	public static double[][] toDoubleArray(Vec[] pt) {
		int ptSize = pt.length;
		double[][] ptF = new double[ptSize][3];
		for (int i = 0; i < ptSize; i++) {
			ptF[i][0] = pt[i].x;
			ptF[i][1] = pt[i].y;
			ptF[i][2] = pt[i].z;

		}

		return ptF;
	}

	/**
	 * identifier for whether the input curve is counterclockwise
	 */
	private final boolean isCCW;
	/**
	 * input polyline represented curve
	 */
	private final Vec[] vertex;
	/**
	 * number of vertex of the input curve
	 */
	private final int numOfVertex;
	/**
	 * minimum required distance between a vertex and its neighbor for taking
	 * the neighbor into consideration when calculating the curvature of this
	 * vertex.
	 */
	private final double errorFixDistance;

	/**
	 * directions of the segments in the input curve
	 */
	private Vec[] directionOfSegment;

	private Vec[] tangentOfVertex;
	private Vec[] normalOfVertex;
	private double[] curvatureOfVertex;
	private boolean[] isConcaveVertex;
	private boolean[] isConvexVertex;

	/**
	 * calculate curvature
	 * 
	 * @param s
	 */
	public CurvatureCalculator(float[][] s) {
		this(toVecArray(s));
	}

	/**
	 * calculate curvature
	 * 
	 * @param s
	 */
	public CurvatureCalculator(double[][] s) {
		this(toVecArray(s));
	}

	/**
	 * calculate curvature
	 * 
	 * @param s
	 */
	public CurvatureCalculator(Vec[] s) {
		this(s, 0);
	}

	/**
	 * calculate curvature
	 * 
	 * @param s
	 * @param errorFixDist_
	 *            minimum required distance between a vertex and its neighbor
	 *            for taking the neighbor into consideration when calculating
	 *            the curvature of this vertex.
	 */
	public CurvatureCalculator(float[][] s, double errorFixDist_) {
		this(toVecArray(s), errorFixDist_);
	}

	/**
	 * calculate curvature
	 * 
	 * @param s
	 * @param errorFixDist_
	 *            minimum required distance between a vertex and its neighbor
	 *            for taking the neighbor into consideration when calculating
	 *            the curvature of this vertex.
	 */
	public CurvatureCalculator(double[][] s, double errorFixDist_) {
		this(toVecArray(s), errorFixDist_);
	}

	/**
	 * calculate curvature
	 * 
	 * @param s
	 * @param errorFixDist_
	 *            minimum required distance between a vertex and its neighbor
	 *            for taking the neighbor into consideration when calculating
	 *            the curvature of this vertex.
	 */
	public CurvatureCalculator(Vec[] s, double errorFixDist_) {
		vertex = s;
		isCCW = ClockwiseManager.isCounterClockwise(vertex);
		errorFixDistance = errorFixDist_;
		numOfVertex = vertex.length;

		analyze();
	}

	private void analyze() {
		directionOfSegment = new Vec[numOfVertex];
		tangentOfVertex = new Vec[numOfVertex];
		normalOfVertex = new Vec[numOfVertex];
		curvatureOfVertex = new double[numOfVertex];
		isConcaveVertex = new boolean[numOfVertex];
		isConvexVertex = new boolean[numOfVertex];

		int len = vertex.length;

		for (int i = 0; i < len; i++) {
			int n = (i + 1) % len;
			directionOfSegment[i] = vertex[n].dup().sub(vertex[i]);
		}

		for (int i = 0; i < len; i++) {
			int n = i;
			int p = (i - 1 + len) % len;

			Vec directionCur = directionOfSegment[n].dup();
			// make current direction long enough
			double distSum = directionCur.len();
			while (distSum < errorFixDistance) {
				n = (n + 1) % len;
				Vec directionNext = directionOfSegment[n];
				distSum += directionNext.len();
				directionCur.add(directionNext);
			}
			// make previous direction long enough
			Vec directionPre = directionOfSegment[p].dup();
			distSum = directionPre.len();
			while (distSum < errorFixDistance) {
				p = (p - 1 + len) % len;
				Vec directionP = directionOfSegment[p];
				distSum += directionP.len();
				directionPre.add(directionP);
			}

			curvatureOfVertex[i] = directionPre.angle(directionCur);// direction[p].angle(direction[i]);
			tangentOfVertex[i] = directionPre.dup().add(directionCur).unit();// direction[p].dup().add(direction[i]).unit();
			if (isCCW) {
				normalOfVertex[i] = tangentOfVertex[i].cross(Vec.zaxis).unit();
			} else {
				normalOfVertex[i] = Vec.zaxis.cross(tangentOfVertex[i]).unit();
			}

			Vec cross = directionPre.cross(directionCur);// direction[p].cross(direction[i]);

			if (isCCW) {
				isConcaveVertex[i] = cross.z < 0;
				isConvexVertex[i] = cross.z > 0;
			} else {
				isConcaveVertex[i] = cross.z > 0;
				isConvexVertex[i] = cross.z < 0;
			}

		}
	}

	/**
	 * 
	 * @param revConvex
	 *            consider convex vertex as negative curvature
	 * @param revConcave
	 *            consider concave vertex as negative curvature
	 * @param reverseList
	 *            true: from small to large, false: from large to small
	 * @return
	 */
	public int[] listCurvature(boolean revConvex, boolean revConcave, boolean reverseList) {
		int[] list = new int[numOfVertex];
		double[] curvature = new double[numOfVertex];

		for (int i = 0; i < numOfVertex; i++) {
			list[i] = i;

			double testAng = getTangentAngle(i, revConvex, revConcave);
			curvature[i] = testAng;
		}

		// sort

		for (int i = numOfVertex - 1; i > 0; i--) {
			for (int j = 0; j < i; j++) {
				if ((reverseList && curvature[list[j + 1]] < curvature[list[j]]) || (!reverseList && curvature[list[j + 1]] > curvature[list[j]])) {
					int temp = list[j];
					list[j] = list[j + 1];
					list[j + 1] = temp;
				}
			}
		}

		return list;
	}

	/**
	 * if the input curve is in counterclockwise order. This relates to the
	 * orientation of normal vectors of vertex in the curve.
	 * <p>
	 * 
	 * 
	 * @return
	 */
	public boolean isCounterClockwise() {
		return isCCW;
	}

	public int getPointNum() {
		return numOfVertex;
	}

	public double[] getTangentAngles(int[] index) {
		double[] out = new double[index.length];
		for (int i = 0; i < index.length; i++) {
			out[i] = curvatureOfVertex[index[i]];
		}
		return out;
	}

	public double getTangentAngle(int index) {
		return curvatureOfVertex[index];
	}

	public double getTangentAngle(int index, boolean revConvex, boolean revConcave) {
		double testAng = curvatureOfVertex[index];

		if (revConvex && isConvexVertex[index])
			testAng *= -1;
		if (revConcave && isConcaveVertex[index])
			testAng *= -1;

		return testAng;
	}

	public Vec[] getPoints(int[] index) {
		Vec[] out = new Vec[index.length];
		for (int i = 0; i < index.length; i++) {
			out[i] = vertex[index[i]];
		}
		return out;
	}

	public Vec getPoint(int index) {
		return vertex[index];
	}

	public Vec[] getTangents(int[] index) {
		Vec[] out = new Vec[index.length];
		for (int i = 0; i < index.length; i++) {
			out[i] = tangentOfVertex[index[i]];
		}
		return out;
	}

	public Vec getTangent(int index) {
		return tangentOfVertex[index].dup();
	}

	public Vec[] getNormals(int[] index) {
		Vec[] out = new Vec[index.length];
		for (int i = 0; i < index.length; i++) {
			out[i] = normalOfVertex[index[i]];
		}
		return out;
	}

	public Vec getNormal(int index) {
		return normalOfVertex[index];
	}

	public boolean isConvex(int index) {
		return isConvexVertex[index];
	}

	public boolean isConcave(int index) {
		return isConcaveVertex[index];
	}
}
