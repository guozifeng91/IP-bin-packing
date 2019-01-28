package layoutIP;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import gzf.Vec;
import igeo.ICircle;
import igeo.ICurve;
import igeo.IG;
import igeo.IText;
import igeo.IVecI;

public class Template {
	public static final GeometryFactory gf = new GeometryFactory();
	public static boolean debug = true;

	private static void checkIG() {
		if (IG.cur() == null)
			IG.init();
	}

	public static void setWeight(Template[] templates, double min, double max, double deg) {
		if (min >= max)
			return;
		double d1 = max - min;

		double[] area = new double[templates.length];
		double minArea = Double.POSITIVE_INFINITY;
		double maxArea = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < templates.length; i++) {
			area[i] = Math.pow(templates[i].shape.getArea(), deg);
			minArea = Math.min(minArea, area[i]);
			maxArea = Math.max(maxArea, area[i]);
		}

		if (minArea >= maxArea) {
			return;
		}

		double d2 = maxArea - minArea;

		for (int i = 0; i < templates.length; i++) {
			templates[i].weight = (area[i] - minArea) / d2;
			templates[i].weight = templates[i].weight * d1 + min;
		}
	}

	public static Polygon toPolygon(ICurve c) {
		int num = c.cpNum();
		Coordinate[] coord = new Coordinate[num];

		for (int i = 0; i < num; i++) {
			IVecI pt = c.cp(i);
			coord[i] = new Coordinate(pt.x(), pt.y());
		}

		LineString loop = gf.createLineString(coord);
		if (!loop.isClosed()) {
			System.err.println("Invalid curve: Unclosed!");
			return null;
		} else if (!loop.isSimple()) {
			System.err.println("Invalid curve: Self-intersect!");
			return null;
		}

		return gf.createPolygon(coord);
	}

	/**
	 * 
	 * @param outer
	 * @return
	 */
	public static Template fromDoubleArrayOpen(Domain d, double[][] outer) {
		Coordinate[] coord = new Coordinate[outer.length + 1];
		for (int i = 0; i < coord.length; i++) {
			int n = i % outer.length;

			coord[i] = new Coordinate(outer[n][0], outer[n][1]);
		}
		return new Template(d, gf.createPolygon(coord), null, null, "");
	}

	public static Template fromDoubleArrayClose(Domain d, double[][] outer) {
		Coordinate[] coord = new Coordinate[outer.length];
		for (int i = 0; i < coord.length; i++) {
			coord[i] = new Coordinate(outer[i][0], outer[i][1]);
		}
		return new Template(d, gf.createPolygon(coord), null, null, "");
	}

	/**
	 * load template from rhino file using IGeo package
	 * <p>
	 * Planar polygons are regarded as the boundaries (both inner and outer) of the
	 * workpieces. Circles are regarded as holes and assigned to the workpieces
	 * according to its positions. Texts are regarded as labels of workpieces.
	 * <p>
	 * An implementation using binary tree
	 * 
	 * @param domain
	 *            where the template will be placed
	 * @param filename
	 * @return
	 */
	public static Template[] loadFromRhino(Domain d, String filename) {
		long t = System.currentTimeMillis();
		/*
		 * binary tree as hierarchical structure of polygons, left branches indicate
		 * polygons in the same level, right branches indicate the sub-polygons.
		 */
		BinaryTreeNode<Polygon> rootNode = null;

		checkIG();
		IG.clear();
		IG.open(filename);

		ArrayList<ICircle> holes = new ArrayList<ICircle>();

		ICurve[] curves = IG.curves();

		for (int i = 0; i < curves.length; i++) {
			if (debug && i % 500 == 0)
				System.out.println("processing " + i + " of " + curves.length);
			if (curves[i].isClosed()) {
				if (curves[i] instanceof ICircle) {
					/* circle, regard as holes, leave it later */
					holes.add((ICircle) curves[i]);
				} else if (curves[i].deg() == 1) {
					/* polygon, regard as boundaries */
					Polygon curPolygon = toPolygon(curves[i]);
					if (curPolygon == null) {
						continue;
					}

					BinaryTreeNode<Polygon> curNode = new BinaryTreeNode<Polygon>(curPolygon);
					if (rootNode == null) {
						/* no tree */
						rootNode = curNode;
					} else {
						/* iterate over left children */
						BinaryTreeNode<Polygon> anotherNode = rootNode.getChildLeft();
						/* if current node has attached to a node */
						boolean attached = false;
						while (anotherNode != null) {
							boolean halt = false;
							/* compare current node and another node */
							Polygon anotherPolygon = anotherNode.getObject();
							if (curPolygon.within(anotherPolygon)) {
								/*
								 * current node is the right child of the another node
								 */
								curNode.attachToRight(anotherNode.getLeafRight());
								attached = true;
								break;
							} else if (anotherPolygon.within(curPolygon)) {
								/* divide another polygon from the tree */
								BinaryTreeNode<Polygon> oldFather = anotherNode.detachFromFather();
								BinaryTreeNode<Polygon> oldChild = anotherNode.detachLeftChild();

								/*
								 * attach another polygon to current node right leaf
								 */
								anotherNode.attachToRight(curNode.getLeafRight());

								if (oldFather == null && oldChild == null) {
									/*
									 * both null, current node as new root node, break
									 */
									attached = true;
									rootNode = curNode;
									break;
								} else if (oldFather == null && oldChild != null) {
									/*
									 * child exist while father does not exist, child replace the father, halt for
									 * one iteration
									 */
									rootNode = oldChild;
									anotherNode = oldChild;
									/*
									 * mark halt as true so that oldChild will be compared with current node in the
									 * next iteration
									 */
									halt = true;
								} else if (oldFather != null && oldChild == null) {
									/*
									 * father exist while child does not exist, attach to father, break;
									 */
									attached = true;
									curNode.attachToLeft(oldFather);
									break;
								} else {
									/*
									 * both exist, link old child to old father, set the anotherNode to oldFather so
									 * the oldChild will be compared in the next iteration. continue the loop
									 */
									oldChild.attachToLeft(oldFather);
									anotherNode = oldFather;
								}
							}

							if (!halt)
								anotherNode = anotherNode.getChildLeft();
						}
						if (!attached) {
							curNode.attachToLeft(rootNode.getLeafLeft());
						}
					}
				}
			}
		}
		if (debug)
			System.out.println("tree complete in " + (System.currentTimeMillis() - t) + " ms");

		ArrayList<Polygon> allPolygons = new ArrayList<Polygon>();

		/* get all polygons */
		BinaryTreeNode<Polygon> node = rootNode;
		do {
			if (node.getChildRight() != null) {
				/* node has sub polygon(right child) ? */
				ArrayList<BinaryTreeNode<Polygon>> allNodes = node.getChildRight().getChildren(true, true);
				allNodes.add(node);
				/* calculate symmetric difference in a much smaller group */
				Geometry g = null;
				for (BinaryTreeNode<Polygon> subNode : allNodes) {
					if (g == null) {
						g = subNode.getObject();
					} else {
						g = g.symDifference(subNode.getObject());
					}
				}

				for (int i = 0; i < g.getNumGeometries(); i++) {
					Geometry g_ = g.getGeometryN(i);
					if (g_ instanceof Polygon) {
						allPolygons.add((Polygon) g_);
					}
				}
			} else {
				allPolygons.add(node.getObject());
			}
		} while ((node = node.getChildLeft()) != null);

		/*
		 * assign holes to polygons
		 */

		boolean[] added = new boolean[holes.size()];
		Point[] center = new Point[holes.size()];

		for (int i = 0; i < center.length; i++) {
			ICircle c = holes.get(i);
			center[i] = gf.createPoint(new Coordinate(c.center().x, c.center().y));
		}

		/*
		 * hole by geometry
		 */
		ArrayList<ArrayList<ICircle>> holePiece = new ArrayList<ArrayList<ICircle>>();
		for (int i = 0; i < allPolygons.size(); i++) {
			Polygon p = allPolygons.get(i);
			ArrayList<ICircle> hp = new ArrayList<ICircle>();
			for (int j = 0; j < holes.size(); j++) {
				if (!added[j] && p.contains(center[j])) {
					added[j] = true;
					hp.add(holes.get(j));
				}
			}

			holePiece.add(hp);
		}

		/*
		 * text by geometry
		 */
		IText[] texts = IG.texts();

		added = new boolean[texts.length];
		center = new Point[texts.length];

		for (int i = 0; i < texts.length; i++) {
			IText txt = texts[i];
			center[i] = gf.createPoint(new Coordinate(txt.center().x, txt.center().y));
		}

		ArrayList<String> labels = new ArrayList<String>();
		for (int i = 0; i < allPolygons.size(); i++) {
			String label = "";
			Polygon p = allPolygons.get(i);

			for (int j = 0; j < texts.length; j++) {
				if (!added[j] && p.contains(center[j])) {
					added[j] = true;
					label += texts[j].text();
				}
			}

			labels.add(label);
		}

		/*
		 * create template
		 */
		Template[] pieces = new Template[allPolygons.size()];

		for (int i = 0; i < pieces.length; i++) {
			Polygon p = allPolygons.get(i);
			ArrayList<ICircle> hp = holePiece.get(i);

			double[][] h = new double[hp.size()][];
			String[] inf = new String[hp.size()];

			for (int j = 0; j < hp.size(); j++) {
				ICircle c = hp.get(j);
				double x = c.center().x;
				double y = c.center().y;
				double r = c.center().dist(c.pt(0));

				h[j] = new double[] { x, y, r };
				inf[j] = c.layer().name();
			}

			String l = labels.get(i);

			pieces[i] = new Template(d, p, h, inf, l);
		}

		IG.clear();

		if (debug) {
			System.out.println(pieces.length + " components loaded in " + (System.currentTimeMillis() - t) + " ms");
			System.out.println("by loadWorkpieceFromRhinoFast()");
		}
		return pieces;
	}

	public final Domain domain;

	public final String label;
	/**
	 * polygon boundary of the workpiece(including both outer and inner boundaries)
	 */
	public final Geometry shape;

	public final double[][] holes; // hole x, y, radius, this part is not yet implemented
	public final String[] holeInf; // hole name, this part is not yet implemented

	/**
	 * instance number, 1 by default
	 */
	public int instNum = 1;

	public double weight = 1;

	public Template(Domain d, Polygon g, double[][] h, String[] hInf, String label_) {
		domain = d;
		/*
		 * move the input geometry to the origin
		 */
		Coordinate centroid = g.getEnvelopeInternal().centre();
		Vec trans = new Vec(-centroid.x, -centroid.y);
		Vec center = new Vec(centroid.x, centroid.y);
		double ang = TransformOp.align(g);

		shape = TransformOp.transform(g, trans, center, ang, false);
		holes = TransformOp.transform(h, trans, center, ang, false);

		holeInf = hInf;
		label = label_;
	}

	/**
	 * discretized template
	 */
	public DiscreteGeometry[] discreteShape;
	public double[] rotation;
	public boolean[] flip;

	public void discretize(int rotationNum, boolean f) {
		int num = rotationNum;
		if (f)
			num *= 2;

		discreteShape = new DiscreteGeometry[num];
		rotation = new double[num];
		flip = new boolean[num];
		for (int i = 0; i < num; i++) {
			flip[i] = i / rotationNum > 0;
			rotation[i] = Math.PI * 2 * (i % rotationNum) / rotationNum;
			discreteShape[i] = new DiscreteGeometry(TransformOp.transform((Geometry) shape.clone(), Vec.origin, Vec.origin, rotation[i], flip[i]), domain.cellSize, domain.cellSize / 3);
		}
	}

	public void discretize(double[] angle, boolean f) {
		int rotationNum = angle.length;
		int num = rotationNum;
		if (f)
			num *= 2;

		discreteShape = new DiscreteGeometry[num];
		rotation = angle;
		flip = new boolean[num];
		for (int i = 0; i < num; i++) {
			flip[i] = i / rotationNum > 0;
			discreteShape[i] = new DiscreteGeometry(TransformOp.transform((Geometry) shape.clone(), Vec.origin, Vec.origin, rotation[i], flip[i]), domain.cellSize, domain.cellSize / 3);
		}
	}
}
