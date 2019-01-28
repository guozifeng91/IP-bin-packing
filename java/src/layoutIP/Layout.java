package layoutIP;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import gzf.Vec;

/**
 * Planned layout
 * 
 * @author guozifeng
 *
 */
public class Layout {
	static class IntegerList extends ArrayList<Integer> {
		private static final long serialVersionUID = 1L;
	}

	public Domain domain;
	public Template[] templates;
	/**
	 * all valid + unused placements
	 * <p>
	 * each placement: int[]{index of template, index of discrete geometry of this
	 * template, x, y index of the anchor cell in the domain}
	 */
	public ArrayList<int[]> placements;

	/**
	 * The placements <b>P<sub>d</sub></b> &sub; <b>P</b> of grid cell <b>d</b>
	 * &isin; <b>D</b>
	 * <p>
	 * placement index = int[x][y][which placement]
	 */
	public IntegerList[][] placements_cell;
	/**
	 * The placements <b>P<sub>t</sub></b> &sub; <b>P</b> of template <b>t</b>
	 * &isin; <b>T</b>
	 * <p>
	 * placement index = int[which template][which placement]
	 */
	public IntegerList[] placements_template;

	/**
	 * integer, index of the result placements
	 */
	// public Integer[] resultInt;
	/**
	 * result placement
	 */
	public int[][] result;

	public Geometry[] resultGeometries;

	/**
	 * create layout
	 * 
	 * @param d
	 * @param t
	 */
	public Layout(Domain d, Template[] t) {
		domain = d;
		templates = t;
	}

	/**
	 * create layout with existed templates and placement
	 * 
	 * @param d
	 * @param t
	 * @param p
	 */
	public Layout(Domain d, Template[] t, ArrayList<int[]> p) {
		domain = d;
		templates = t;
		placements = p;
	}

	public void enumerate(double[] angle, boolean flip) {
		if (Template.debug) {
			System.out.println("Enumerating...");
		}
		placements = new ArrayList<int[]>();

		int rotationNum = angle.length;

		int num = flip ? rotationNum * 2 : rotationNum;
		/*
		 * discretize
		 */
		for (Template t : templates) {
			t.discretize(angle, flip);
		}

		/*
		 * enumerate
		 */
		for (int i = 0; i < templates.length; i++) {
			for (int j = 0; j < num; j++) {
				int[][] tempCell = templates[i].discreteShape[j].cells;
				int w = templates[i].discreteShape[j].xNum;
				int h = templates[i].discreteShape[j].yNum;

				for (int u = 0; u < domain.xNum; u++) {
					for (int v = 0; v < domain.yNum; v++) {
						// out of boundary?
						if (u + w > domain.xNum || v + h > domain.yNum)
							continue;

						// is valid?
						boolean valid = true;
						for (int k = 0; k < tempCell.length; k++) {
							int x = tempCell[k][0];
							int y = tempCell[k][1];
							double val = templates[i].discreteShape[j].maskFloat[x][y];

							if (!domain.isInsideDomain(x + u, y + v, val)) {
								valid = false;
								break;
							}
						}

						if (valid) {
							placements.add(new int[] { i, j, u, v });
						}
					}
				}
			}
		}
	}

	/**
	 * enumerate all the placements, only needed for the first layout (the rest will
	 * reuse the results)
	 * 
	 * @param rotationNum
	 * @param flip
	 */
	public void enumerate(int rotationNum, boolean flip) {
		if (Template.debug) {
			System.out.println("Enumerating...");
		}
		placements = new ArrayList<int[]>();

		int num = flip ? rotationNum * 2 : rotationNum;
		/*
		 * discretize
		 */
		for (Template t : templates) {
			t.discretize(rotationNum, flip);
		}

		/*
		 * enumerate
		 */
		for (int i = 0; i < templates.length; i++) {
			for (int j = 0; j < num; j++) {
				int[][] tempCell = templates[i].discreteShape[j].cells;
				int w = templates[i].discreteShape[j].xNum;
				int h = templates[i].discreteShape[j].yNum;

				for (int u = 0; u < domain.xNum; u++) {
					for (int v = 0; v < domain.yNum; v++) {
						// out of boundary?
						if (u + w > domain.xNum || v + h > domain.yNum)
							continue;

						// is valid?
						boolean valid = true;
						for (int k = 0; k < tempCell.length; k++) {
							int x = tempCell[k][0];
							int y = tempCell[k][1];
							double val = templates[i].discreteShape[j].maskFloat[x][y];

							if (!domain.isInsideDomain(x + u, y + v, val)) {
								valid = false;
								break;
							}
						}

						if (valid) {
							placements.add(new int[] { i, j, u, v });
						}
					}
				}
			}
		}
	}

	public void analysis() {
		if (Template.debug) {
			System.out.println("Analyzing...");
		}

		placements_template = new IntegerList[templates.length];
		placements_cell = new IntegerList[domain.xNum][domain.yNum];

		for (int i = 0; i < templates.length; i++)
			placements_template[i] = new IntegerList();

		for (int u = 0; u < domain.xNum; u++) {
			for (int v = 0; v < domain.yNum; v++) {
				placements_cell[u][v] = new IntegerList();
			}
		}

		/*
		 * for each placements
		 */
		for (int i = 0; i < placements.size(); i++) {
			int[] p = placements.get(i);
			placements_template[p[0]].add(i);

			// cell in domain = cell in template + anchor
			for (int[] cells : templates[p[0]].discreteShape[p[1]].cells) {
				placements_cell[cells[0] + p[2]][cells[1] + p[3]].add(i);
			}
		}
	}

	/**
	 * Use Integer Programming to solve this layout plan by solve such equations:
	 * <p>
	 * Maximize &sum; <b>w*P</b>
	 * <p>
	 * <b> Subject to.</b> <br>
	 * For each <b>d</b> &isin; <b>D</b>, &sum;<b>P<sub>d</sub> &le; 1</b> <br>
	 * For each <b>t</b> &isin; <b>T</b>, &sum;<b>P<sub>t</sub> &le;
	 * n<sub>t</sub></b>
	 * <p>
	 * where <b>w</b> are weights of placements, which can be accessed through
	 * {@link Placement#template} and {@link Template#getWeight()}.<br>
	 * <b>P</b> is the set of all placements in <code>LayoutPlan</code> and can be
	 * accessed through array <code>{@link Layout#placements}</code>, <br>
	 * <b>D</b> is the domain <code>{@link Layout#domain}</code> that indicates the
	 * dimension of planning area, <br>
	 * <b>P<sub>d</sub></b> is the set of placements of grid <b>d</b> &isin;
	 * <b>D</b> and its index in <b>P</b> can be accessed through
	 * <code>{@link Layout#placements_grid}</code>,<br>
	 * <b>T</b> is the set of all templates,<br>
	 * <b>P<sub>t</sub></b> is the set of placements of template <b>t</b> &isin;
	 * <b>T</b> and its index in <b>P</b> can be accessed through
	 * <code>{@link Layout#placements_template}</code>
	 * <p>
	 * 
	 * @param solver
	 */
	public void solve(IPSolver solver) {
		if (result == null) {
			Integer[] resultInt = solver.solve(this);
			if (resultInt != null) {
				int resultNum = resultInt.length;

				result = new int[resultNum][];
				resultGeometries = new Geometry[result.length];

				for (int i = 0; i < resultNum; i++) {
					// add result placement to the list
					result[i] = placements.get(resultInt[i]);
					// reduce the template instance number
					templates[result[i][0]].instNum--;

					int[] p = result[i];
					DiscreteGeometry geo = templates[p[0]].discreteShape[p[1]];
					Vec trans = domain.getTranslationInDomain(geo, p[2], p[3]);
					resultGeometries[i] = TransformOp.transform((Geometry) geo.g.clone(), trans, new Vec(), 0, false);
				}
			} else {
				result = new int[0][];
			}
		}
	}

	public void postprocess(int iteration, double step, boolean simple) {
		Geometry domainShape = null;
		Geometry domainBound = null;

		if (domain.mask != null) {
			domainShape = TransformOp.transform((Geometry)domain.mask.g.clone(), domain.getTranslationInDomain(domain.mask, 0, 0), new Vec(), 0, false);
			domainBound = domainShape.getBoundary();
		}
		Geometry[] resultBoundary = new Geometry[result.length];
		Vec[] resultTrans = new Vec[result.length];

		double w = domain.xNum * domain.cellSize;
		double h = domain.yNum * domain.cellSize;

		for (int i = 0; i < result.length; i++) {
			resultTrans[i] = new Vec();
			resultBoundary[i] = resultGeometries[i].getBoundary();
		}

		for (int iter = 0; iter < iteration; iter++) {
			// buffer data
			Coordinate[][] coords = new Coordinate[result.length][];
			Point[][] pts = new Point[result.length][];
			Envelope[] env = new Envelope[result.length];
			for (int i = 0; i < result.length; i++) {
				coords[i] = resultGeometries[i].getCoordinates();
				pts[i] = new Point[coords[i].length];
				for (int j = 0; j < coords[i].length; j++) {
					pts[i][j] = Template.gf.createPoint(coords[i][j]);
				}

				env[i] = resultGeometries[i].getEnvelopeInternal();
			}

			// within boundary
			if (domain.mask == null) {
				for (int i = 0; i < result.length; i++) {
					resultTrans[i].x += Math.max(0, -env[i].getMinX()); // if minX < 0
					resultTrans[i].y += Math.max(0, -env[i].getMinY()); // if minY < 0

					resultTrans[i].x -= Math.max(0, env[i].getMaxX() - w); // if maxX - w > 0
					resultTrans[i].y -= Math.max(0, env[i].getMaxY() - h); // if maxY - h > 0
				}
			} else {
				for (int i = 0; i < result.length; i++) {
					if (resultGeometries[i].intersects(domainBound)) {
						keepDist(coords[i], pts[i], domainShape, domainBound, resultTrans[i], step);
					}
				}
			}

			boolean move = false;
			// keep dist
			for (int i = 0; i < result.length; i++) {
				for (int j = i + 1; j < result.length; j++) {
					if (resultGeometries[i].intersects(resultGeometries[j])) {
						move = true;
						if (simple) {
							// simple keep distance
							Coordinate c1 = resultGeometries[i].getCentroid().getCoordinate();
							Coordinate c2 = resultGeometries[j].getCentroid().getCoordinate();
							// movement from i to j
							Vec movement = new Vec(c2.x - c1.x, c2.y - c1.y).unit().mul(step);
							resultTrans[i].sub(movement);
							resultTrans[j].add(movement);
						} else {
							// point-dependent keep distance
							Geometry int_set = resultGeometries[i].intersection(resultBoundary[j]);
							if (!int_set.isEmpty())
								keepDist2(coords[i], pts[i], resultGeometries[j], int_set, resultTrans[i], resultTrans[j], step);

							int_set = resultGeometries[j].intersection(resultBoundary[i]);
							if (!int_set.isEmpty())
								keepDist2(coords[j], pts[j], resultGeometries[i], int_set, resultTrans[j], resultTrans[i], step);
						}

					}
				}
			}

			// update result geometries
			for (int i = 0; i < result.length; i++) {
				int[] p = result[i];
				DiscreteGeometry geo = templates[p[0]].discreteShape[p[1]];
				Vec trans = domain.getTranslationInDomain(geo, p[2], p[3]);
				trans.add(resultTrans[i]);
				resultGeometries[i] = TransformOp.transform((Geometry) geo.g.clone(), trans, new Vec(), 0, false);
			}

			if (!move)
				break;
		}
	}

	void keepDist(Coordinate[] coord, Point[] pt, Geometry poly, Geometry bound, Vec v, double step) {
		for (int id = 0; id < coord.length; id++) {
			if (!poly.covers(pt[id])) { // point outside poly (domain geometry)
				Coordinate c1 = coord[id];
				// closest point on the boundary of domain
				Coordinate c2 = DistanceOp.nearestPoints(pt[id], bound)[1];
				// to boundary (towards inside)
				Vec movement = new Vec(c2.x - c1.x, c2.y - c1.y);
				v.add(movement);
			}
		}
	}

	void keepDist2(Coordinate[] coord, Point[] pt, Geometry poly, Geometry bound, Vec v1, Vec v2, double step) {
		for (int id = 0; id < coord.length; id++) {
			if (poly.covers(pt[id])) { // point inside poly
				Coordinate c1 = coord[id];
				// closest point on the boundary
				Coordinate c2 = DistanceOp.nearestPoints(pt[id], bound)[1];
				// to another (towards outside)
				Vec movement = new Vec(c2.x - c1.x, c2.y - c1.y).unit().mul(step);
				v1.add(movement);
				v2.sub(movement);
			}
		}
	}

	/**
	 * get remained placements (use all templates as remaining templates)
	 * 
	 * @return
	 */
	public ArrayList<int[]> getRemainPlacements() {
		ArrayList<int[]> rp = new ArrayList<int[]>();
		for (int[] p : placements) {
			if (templates[p[0]].instNum > 0)
				rp.add(p);
		}
		return rp;
	}

	/**
	 * clear buffer data
	 */
	public void clear() {
		placements_template = null;
		placements = null;
		placements_cell = null;
	}
}
