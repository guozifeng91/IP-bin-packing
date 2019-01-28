package layoutIP;

import com.vividsolutions.jts.geom.Geometry;

import gzf.Vec;

public class Domain {
	public boolean useFloatMask = true;

	public double distPowerRatio = 1;
	/**
	 * the number of grid cells in the domain, on x and y directions.
	 */
	public final int xNum, yNum;
	/**
	 * the size of grid cells in the domain, on x and y directions.
	 */
	public final double cellSize;

	/**
	 * irregular boundary
	 */
	public final DiscreteGeometry mask;

	/**
	 * construct a domain by specifying the dimension and the size of cells
	 * 
	 * @param xNum_
	 * @param yNum_
	 * @param cellSize_
	 */
	public Domain(int xNum_, int yNum_, double cellSize_) {
		xNum = xNum_;
		yNum = yNum_;
		cellSize = cellSize_;
		mask = null;
	}

	/**
	 * construct a domain from a mask polygon. we only need one grid size?
	 * <p>
	 * addin in 2018-10-24, for irregular boundary
	 * 
	 * @param mask_
	 * @param cellSize
	 */
	public Domain(Geometry mask_, double cellSize_) {
		mask = new DiscreteGeometry(mask_, cellSize_, 0);
		cellSize = cellSize_;
		xNum = mask.xNum;
		yNum = mask.yNum;
	}

	public boolean isInsideDomain(int i, int j, double v) {
		if (useFloatMask) {
			return isInsideDomainFloat(i, j, v);
		} else {
			return isInsideDomainBool(i, j);
		}
	}

	/**
	 * if a grid cell is inside the domain, boundary size will not be checked
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public boolean isInsideDomainBool(int i, int j) {
		if (mask == null)
			return true;
		return mask.maskBool[i][j];
	}

	public boolean isInsideDomainFloat(int i, int j, double v) {
		if (mask == null)
			return true;
		double v1 = 1 - mask.maskFloat[i][j];
		return Math.pow(v1, distPowerRatio) + Math.pow(v, distPowerRatio) <= 1;
	}

	/**
	 * get the exact position of cell (i, j)
	 * 
	 * @param i
	 * @param j
	 * @return
	 */
	public double[] getPosition(int i, int j) {
		return new double[] { (i + 0.5) * cellSize, (j + 0.5) * cellSize };
	}

	/**
	 * get the exact position of a cell
	 * 
	 * @param pos
	 *            cell index as an array: int{i, j}
	 * @return
	 */
	public double[] getPosition(int[] pos) {
		return getPosition(pos[0], pos[1]);
	}

	/**
	 * get the translation of original continuous geometry when puting a discrete
	 * one into domain
	 * 
	 * @param geo
	 * @param u
	 * @param v
	 * @return
	 */
	public Vec getTranslationInDomain(DiscreteGeometry geo, int u, int v) {
		// align geometry to grid
		double tx = -geo.env.getMinX() + geo.biasX;
		double ty = -geo.env.getMinY() + geo.biasY;

		// align grid to domain
		tx += u * cellSize;
		ty += v * cellSize;
		return new Vec(tx, ty);
	}
}
