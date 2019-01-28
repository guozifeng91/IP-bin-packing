package layoutIP.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

import igeo.ICircle;
import igeo.ICurve;
import igeo.IG;
import igeo.IVecI;
import layoutIP.BinaryTreeNode;
import layoutIP.Dataset;
import layoutIP.Domain;
import layoutIP.GurobiSolver;
import layoutIP.Layout;
import layoutIP.Template;
import layoutIP.processing.LayoutRender;
import layoutLPRender.MGraphics;
import processing.core.PApplet;
import processing.data.JSONObject;

public class TestPlan_Mask extends PApplet {
	Domain domain;
	Template[] templates;
	LayoutRender render;
	ArrayList<Layout> layouts = new ArrayList<Layout>();

	public void setup() {
		size(1200, 600, P3D);

		Geometry geo = null;

		String filemask = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\data\\mask.3dm";
		IG.init();

		IG.open(filemask);
		ICurve[] curves = IG.curves();

		for (ICurve c : curves) {
			Polygon p = toPolygon(c);
			if (geo == null)
				geo = p;
			else
				geo = geo.symDifference(p);
		}
		IG.clear();

		domain = new Domain(geo, 60);
		domain.distPowerRatio = 1;
		domain.useFloatMask = true;

		String file = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\data\\char.3dm";
		IG.open(file);
		curves = IG.curves();
		geo = null;
		for (ICurve c : curves) {
			c.scale(0.8);
			Polygon p = toPolygon(c);
			if (geo == null)
				geo = p;
			else
				geo = geo.symDifference(p);
		}
		IG.clear();

		int polyNum = geo.getNumGeometries();
		templates = new Template[polyNum];
		for (int i = 0; i < polyNum; i++) {
			templates[i] = new Template(domain, (Polygon) geo.getGeometryN(i), null, null, "");
		}

		Template.setWeight(templates, 0.01, 1, 2);

		ArrayList<int[]> rp = null;
		Layout layout = null;
		do {
			println("processing layout " + layouts.size());
			if (rp == null) {
				layout = new Layout(domain, templates);
				layout.enumerate(4, false);
			} else {
				layout = new Layout(domain, templates, rp);
			}
			layout.analysis();
			layout.solve(new GurobiSolver(2, 600));
			layout.postprocess(300, 0.5, false);

			rp = layout.getRemainPlacements();

			layouts.add(layout);
			
			//if (layouts.size()==3) break;
		} while (rp.size() > 0);

		render = new LayoutRender(this);

		String path = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\graphics\\";
		String saveto = "mask_600_mid_IP";

		JSONObject json = MGraphics.writeJSON(layouts, 6, domain.cellSize * domain.xNum + 100, domain.cellSize * domain.yNum + 100);
		json.save(new File(path + saveto + ".json"), "");
	}

	public void draw() {
		background(255);
		noFill();
		stroke(0);
		scale(0.5f);

		Layout layout = layouts.get(mouseX * layouts.size() / width);
		strokeWeight(1);
		render.drawDomain(domain);
		strokeWeight(3);
		render.drawLayout(layout);

		pushStyle();
		stroke(255, 0, 0);
		render.drawLayoutPostprocessing(layout);
		popStyle();
	}

	public static final GeometryFactory gf = new GeometryFactory();
	public static boolean debug = true;

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
}
