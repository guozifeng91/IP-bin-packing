package layoutIP.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * Processing render to draw JTS geometries.
 * 
 * author: LI Biao (15/05/2015)
 * 
 * 
 * */
public class JTSRender {
	PGraphics app;

	public JTSRender(PApplet app) {
		this.app = app.g;
	}
	
	public JTSRender(PGraphics app) {
		this.app = app;
	}

	public void drawFill(Geometry g) {
		int num = g.getNumGeometries();
		for (int i = 0; i < num; i++) {
			Geometry gg = g.getGeometryN(i);
			int dim = g.getDimension();
			if (dim == 2) {
				drawDim2(gg);
			}
		}
	}

	public void drawBoundary(Geometry g) {
		int num = g.getNumGeometries();
		// System.out.println("geo num " + num);
		for (int i = 0; i < num; i++) {
			Geometry gg = g.getGeometryN(i);
			int dim = g.getDimension();

			// System.out.println("dim " + dim);

			if (dim == 0) {
				drawDim0(gg);
			} else if (dim == 1) {
				drawDim1(gg);
			} else if (dim == 2) {
				drawDimBoundary2(gg);
			}
		}
	}

	private void drawDimBoundary2(Geometry g) {
		Coordinate[] coords = g.getCoordinates();
		if (coords == null) {
			return;
		}
		Polygon p = (Polygon) g;
		/*
		 * outer loop
		 */
		Coordinate[] outers = p.getExteriorRing().getCoordinates();

		app.beginShape();
		for (Coordinate c : outers) {
			app.vertex((float) c.x, (float) c.y, (float) (Double.isNaN(c.z) ? 0 : c.z));
		}
		app.endShape(PConstants.CLOSE);
		/*
		 * inner loop
		 */
		int intNum = p.getNumInteriorRing();

		for (int i = 0; i < intNum; i++) {
			LineString ls = p.getInteriorRingN(i);
			Coordinate[] inner = ls.getCoordinates();
			app.beginShape();
			for (int k = 0; k < inner.length; k++) {
				// for (Coordinate in : inner) {
				Coordinate in = inner[k];
				app.vertex((float) in.x, (float) in.y, (float) (Double.isNaN(in.z) ? 0 : in.z));
				// }
			}
			app.endShape(PConstants.CLOSE);
		}
	}

	private void drawDim2(Geometry g) {
		Coordinate[] coords = g.getCoordinates();
		if (coords == null) {
			return;
		}

		/*
		 * fill polygon
		 */

		app.beginShape();
		for (Coordinate c : coords) {
			app.vertex((float) c.x, (float) c.y, (float) (Double.isNaN(c.z) ? 0 : c.z));
		}
		app.endShape(PConstants.CLOSE);

	}

	private void drawDim1(Geometry g) {
		Coordinate[] coords = g.getCoordinates();
		if (coords == null) {
			return;
		}

		if (coords.length == 2) {
			app.line((float) coords[0].x, (float) coords[0].y, (float) (Double.isNaN(coords[0].z) ? 0 : coords[0].z), (float) coords[1].x, (float) coords[1].y,
					(float) (Double.isNaN(coords[1].z) ? 0 : coords[1].z));
		} else {
			app.beginShape();
			for (Coordinate c : coords) {
				app.vertex((float) c.x, (float) c.y, (float) (Double.isNaN(c.z) ? 0 : c.z));
			}
			app.endShape();
		}
	}

	private void drawDim0(Geometry g) {
		Coordinate[] coords = g.getCoordinates();
		if (coords == null) {
			return;
		}

		app.pushMatrix();
		app.translate((float) coords[0].x, (float) coords[0].y, (float) (Double.isNaN(coords[0].z) ? 0 : coords[0].z));
		app.box(4);
		app.popMatrix();
	}
}
