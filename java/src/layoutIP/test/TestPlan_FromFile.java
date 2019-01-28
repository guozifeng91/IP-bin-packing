package layoutIP.test;

import java.io.File;
import java.util.ArrayList;

import layoutIP.Domain;
import layoutIP.GurobiSolver;
import layoutIP.Layout;
import layoutIP.Template;
import layoutIP.processing.LayoutRender;
import layoutLPRender.MGraphics;
import processing.core.PApplet;
import processing.data.JSONObject;

public class TestPlan_FromFile extends PApplet {
	Domain domain;
	Template[] templates;
	LayoutRender render;
	ArrayList<Layout> layouts = new ArrayList<Layout>();

	public void setup() {
		size(1200, 600, P3D);

		String file = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\data\\char.3dm";
		domain = new Domain(30, 15, 80);
		domain.distPowerRatio = 1;
		domain.useFloatMask = true;

		templates = Template.loadFromRhino(domain, file);

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
			layout.solve(new GurobiSolver(2, 360));
			layout.postprocess(300, 0.5,true);

			rp = layout.getRemainPlacements();

			layouts.add(layout);
		} while (rp.size() > 0);

		render = new LayoutRender(this);
		
		String path = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\graphics\\";
		String saveto = "char_IP";

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

}
