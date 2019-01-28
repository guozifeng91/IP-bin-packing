package layoutIP;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * discrete representation of polygon
 * 
 * @author guozifeng
 *
 */
public class DiscreteGeometry {
	private static final GeometryFactory gf = new GeometryFactory();
	/**
	 * continuous geometry
	 */
	public final Geometry g;
	/**
	 * envelope (AABB)
	 */
	public final Envelope env;

	/**
	 * bias that align the center of grid and the envelope
	 */
	public final double biasX, biasY;

	/**
	 * discrete geometry
	 */
	public final boolean[][] maskBool;
	public final float[][] maskFloat;

	/**
	 * index of all occupied cells: int[which grid][0 for x index, 1 for y index]
	 */
	public final int[][] cells;
	/**
	 * size of the grid cells
	 */
	public final int xNum, yNum;
	/**
	 * size of the cell
	 */
	public final double cellSize;

	public DiscreteGeometry(Geometry g_, double size, double thres) {
		g = g_;
		cellSize = size;
		env = g.getEnvelopeInternal();

		double w = env.getWidth();
		double h = env.getHeight();

		xNum = (int) Math.ceil(w / cellSize);
		yNum = (int) Math.ceil(h / cellSize);

		biasX = (xNum * cellSize - w) / 2;
		biasY = (yNum * cellSize - h) / 2;

		ArrayList<int[]> cells = new ArrayList<int[]>();

		maskBool = new boolean[xNum][yNum];
		maskFloat = new float[xNum][yNum];

		Geometry gBuf = g.getBoundary();
		double distToBoundary;

		for (int i = 0; i < xNum; i++) {
			for (int j = 0; j < yNum; j++) {
				double[] pos = getPosition(i, j);
				// move the grid to the origin of AABB
				pos[0] += env.getMinX();
				pos[1] += env.getMinY();
				// align grid center to the AABB center
				pos[0] -= biasX;
				pos[1] -= biasY;

				// when dist to geo < cellSize / 2
				maskFloat[i][j] = (float) g.distance(gf.createPoint(new Coordinate(pos[0], pos[1])));

				if (maskFloat[i][j] <= cellSize / 2) {
					// map ( -cellSize / 2 , cellSize / 2 ) to (1, 0)
					distToBoundary = Math.min(cellSize / 2, gBuf.distance(gf.createPoint(new Coordinate(pos[0], pos[1]))));
					if (maskFloat[i][j] == 0) {
						// inside
						distToBoundary = cellSize / 2 - distToBoundary;
					} else {
						distToBoundary += cellSize / 2;
					}

					maskFloat[i][j] = (float) (1 - distToBoundary / cellSize);
				} else {
					maskFloat[i][j] = 0;
				}

				maskBool[i][j] = g.distance(gf.createPoint(new Coordinate(pos[0], pos[1]))) <= thres;

				if (maskBool[i][j] || maskFloat[i][j] > 0) {
					cells.add(new int[] { i, j });
				}
			}
		}

		this.cells = cells.toArray(new int[cells.size()][]);
	}

	public double[] getPosition(int i, int j) {
		return new double[] { (i + 0.5) * cellSize, (j + 0.5) * cellSize };
	}
}
