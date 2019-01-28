package layoutIP.test;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import layoutIP.Dataset;
import layoutIP.Domain;
import layoutIP.GurobiSolver;
import layoutIP.Layout;
import layoutIP.Template;
import layoutIP.processing.LayoutRender;
import layoutLPRender.MGraphics;
import processing.core.PApplet;
import processing.data.JSONObject;
import reference.pack.M;

public class TestPlan_Benchmark extends PApplet {
	Domain domain;
	Template[] templates;
	LayoutRender render;
	ArrayList<Layout> layouts = new ArrayList<Layout>();

	public void setup() {
		size(1200, 600, P3D);

		int seed = 4346;
		Random ran = new Random(seed);
		
//		int polyNum = 25;
//		double[][][] randompolys = Dataset.getRandomPolygons(ran, Dataset.swim(), polyNum, 0.5, 0, false);

//		int polyNum = 40;
//		double[][][] randompolys = Dataset.getRandomPolygons(ran, Dataset.fu(), polyNum, 30, 20, false);
		
//		int polyNum = 38;
//		double[][][] randompolys = Dataset.getRandomPolygons(ran, Dataset.han(), polyNum, 30, 20, false);		
		
		
//		int polyNum = 38;
//		double[][][] randompolys = Dataset.getRandomPolygons(ran, Dataset.mao(), polyNum, 0.7, 0.3, false);	
		
		int polyNum = 100;
		double[][][] randompolys = Dataset.getRandomPolygons(ran, Dataset.poly(), polyNum, 10,0, false);	
		
		domain = new Domain(30, 15, 80);
		domain.distPowerRatio = 1;
		domain.useFloatMask = true;

		templates = new Template[randompolys.length];
		for (int i = 0; i < templates.length; i++)
			templates[i] = Template.fromDoubleArrayOpen(domain, randompolys[i]);

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
			layout.solve(new GurobiSolver(4, 180));
			layout.postprocess(300, 0.5, false);

			rp = layout.getRemainPlacements();
			layout.clear(); // release memory
			layouts.add(layout);
		} while (rp.size() > 0);

		render = new LayoutRender(this);

		String path = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\graphics\\";
		String saveto = "tiny_IP";

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
