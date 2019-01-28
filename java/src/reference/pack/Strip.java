package reference.pack;

import java.util.ArrayList;

//Hao Hua, Southeast University, whitegreen@163.com

// a template / geometry / polygon instance
public class Strip implements Comparable<Strip> {
	public double[][] outps; // offset & add edge points , as referent point of placement, clock-wise
	public final double[][] inps; // original polygon, for 1. convex, 2. intersection
	public final double inarea;
	public double[] trigo; // cos & sin
	public double[] position;
	public final int id;

	public Strip(int id, double[][] original_poly, double offset, Double segment_max_length) {
		this.id = id;
		if (offset <= 0)
			throw new RuntimeException();
		double area = M.area(original_poly);
		
		// to clockwise
		if (0 < area) {
			inps = M.clone(original_poly);
		} else {
			inps = new double[original_poly.length][];
			for (int i = 0; i < inps.length; i++)
				inps[i] = original_poly[inps.length - 1 - i].clone();
		}
		inarea = Math.abs(area);
		
		// offset
		outps = M.offset(offset, inps); // clockwise

		// cut long segments
		if (null != segment_max_length) {
			ArrayList<double[]> list = new ArrayList<double[]>();
			for (int i = 0; i < outps.length; i++) {
				double[] pa = outps[i];
				double[] pb = outps[(i + 1) % outps.length];
				list.add(pa);
				double dis = M.dist(pa, pb);
				if (segment_max_length < dis) {
					int num = 1 + (int) (dis / segment_max_length);
					for (int j = 1; j < num; j++) {
						double s = (double) j / num;
						list.add(M.between(s, pa, pb));
					}
				}
			} // for
			outps = new double[list.size()][];
			for (int i = 0; i < outps.length; i++)
				outps[i] = list.get(i);
		} // if
	}

	// compare by area
	public int compareTo(Strip pl) {
		if (this.inarea > pl.inarea)
			return 1; // return 1 for ascendant sorting
		else if (this.inarea < pl.inarea)
			return -1;
		else
			return 0;
	}

	// apply rotation and translation
	public void fix_rotate_move(double[] cossin, double[] dv) { // finalize
		trigo = cossin.clone();
		position = dv.clone();
		double cos = trigo[0];
		double sin = trigo[1];
		for (int i = 0; i < inps.length; i++) {
			double[] p = inps[i];
			double x = cos * p[0] - sin * p[1];
			double y = sin * p[0] + cos * p[1];
			inps[i] = new double[] { dv[0] + x, dv[1] + y };
		}
		for (int i = 0; i < outps.length; i++) {
			double[] p = outps[i];
			double x = cos * p[0] - sin * p[1];
			double y = sin * p[0] + cos * p[1];
			outps[i] = new double[] { dv[0] + x, dv[1] + y };
		}
	}

}
