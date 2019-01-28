package layoutIP.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import layoutIP.Domain;
import layoutIP.GurobiSolver;
import layoutIP.Layout;
import layoutIP.Template;
import layoutIP.processing.LayoutRender;
import layoutLPRender.MGraphics;
import processing.core.PApplet;
import processing.data.JSONObject;
import reference.pack.M;

public class TestPlan_HuaSet extends PApplet {
	Domain domain;
	Template[] templates;
	LayoutRender render;
	ArrayList<Layout> layouts = new ArrayList<Layout>();

	int seed = 4346;
	private Random ran = new Random(seed);
	int polyNum = 50;

	private double[][] shake(double s, double[][] ps) {
		double[][] arr = new double[ps.length][];
		double sng = ran.nextBoolean() ? s : (-s); // mirror
		double theta = ran.nextDouble() * 2 * PI;
		double cos = Math.cos(theta);
		double sin = Math.sin(theta);
		for (int i = 0; i < ps.length; i++) {
			double[] p = M.scale(sng, ps[i]);
			p[0] += (ran.nextDouble() - 0.5) * 0.25 * s;
			p[1] += (ran.nextDouble() - 0.5) * 0.25 * s;
			double x = cos * p[0] + sin * p[1];
			double y = -sin * p[0] + cos * p[1];
			arr[i] = new double[] { x, y };
		}
		return arr;
	}

	private double[][][] randomPolygons(int num) {
		double[][][] protos = new double[6][][];
		protos[0] = new double[][] { { 0, 0 }, { 0, 10 }, { 10, 10 }, { 10, 0 } };
		protos[1] = new double[][] { { 0, 0 }, { 0, 16 }, { 1, 16 }, { 1, 0 } };
		protos[2] = new double[][] { { 0, 0 }, { 0, 12 }, { 9, 6 } };
		protos[3] = new double[][] { { 0, 0 }, { 0, 2 }, { 8, 2 }, { 8, 8 }, { 0, 8 }, { 0, 10 }, { 10, 10 }, { 10, 0 } };
		protos[4] = new double[][] { { 0, 0 }, { 0, 2 }, { 6, 2 }, { 6, 12 }, { 8, 12 }, { 8, 0 } };
		protos[5] = new double[][] { { 0, 0 }, { 0, 5 }, { 4, 1 }, { 8, 5 }, { 4, 10 }, { 0, 6 }, { 0, 11 }, { 9, 11 }, { 9, 0 } };

		double[][][] polys = new double[num][][];
		for (int i = 0; i < num; i++) {
			double[][] pl =protos[ran.nextInt(protos.length)];
			// double[][] pl = protos[3];
			double sc = 8 + ran.nextDouble() * 12; // 8+ ran.nextDouble()*12 10+ ran.nextDouble()*20
			polys[i] = shake(3 * sc, pl); //
		}
		for (int i = 0; i < num; i++) {
			double[][] poly = polys[i];
			double dx = ran.nextDouble() * 1000;
			double dy = ran.nextDouble() * 1000;
			for (double[] p : poly) {
				M._add(p, new double[] { dx, dy });
			}
		}
		return polys;
	}

	public void setup() {
		size(1200, 600, P3D);

		domain = new Domain(30, 15, 80);
		domain.distPowerRatio = 1;
		domain.useFloatMask = true;

		double[][][] polys = randomPolygons(polyNum);
		templates = new Template[polys.length];
		for (int i = 0; i < templates.length; i++)
			templates[i] = Template.fromDoubleArrayOpen(domain, polys[i]);

		Template.setWeight(templates, 0.01, 1, 2);

		ArrayList<int[]> rp = null;
		Layout layout = null;
		do {
			println("processing layout " + layouts.size());
			if (rp == null) {
				layout = new Layout(domain, templates);
				layout.enumerate(8, false);
			} else {
				layout = new Layout(domain, templates, rp);
			}
			layout.analysis();
			layout.solve(new GurobiSolver(4, 360));
			layout.postprocess(300, 0.5, false);

			rp = layout.getRemainPlacements();
			layout.clear(); // release memory
			layouts.add(layout);
		} while (rp.size() > 0);

		render = new LayoutRender(this);

		String path = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\graphics\\";
		String saveto = "test2_8_direction_IP";

		JSONObject json = MGraphics.writeJSON(layouts, 3, domain.cellSize * domain.xNum + 100, domain.cellSize * domain.yNum + 100);
		json.save(new File(path + saveto + ".json"), "");
	}

	public void draw() {
		background(255);
		noFill();
		stroke(0);
		scale(0.5f);

		Layout layout = layouts.get(max(0, min(layouts.size() - 1, mouseX * layouts.size() / width)));
		strokeWeight(1);
		render.drawDomain(domain);
		strokeWeight(3);
		render.drawLayout(layout);

		pushStyle();
		stroke(255, 0, 0);
		render.drawLayoutPostprocessing(layout);
		popStyle();
	}
}
