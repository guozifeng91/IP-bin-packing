package layoutIP.processing;

import com.vividsolutions.jts.geom.Geometry;

import gzf.Vec;
import layoutIP.DiscreteGeometry;
import layoutIP.Domain;
import layoutIP.Layout;
import processing.core.PApplet;
import processing.core.PGraphics;

public class LayoutRender {
	JTSRender render;
	PGraphics app;

	public LayoutRender(PApplet app) {
		this.app = app.g;
		render = new JTSRender(app);
	}

	public LayoutRender(PGraphics app) {
		this.app = app;
		render = new JTSRender(app);
	}

	public void drawDomain(Domain d) {
		float w = (float) d.cellSize * d.xNum;
		float h = (float) d.cellSize * d.yNum;
		app.rect(0, 0, w, h);
		for (int i = 0; i < d.xNum; i++) {
			for (int j = 0; j < d.yNum; j++) {
				app.rect(i * (float) d.cellSize, j * (float) d.cellSize, (float) d.cellSize, (float) d.cellSize);
			}
		}
		// draw mask if there is one
		if (d.mask != null) {
			app.pushStyle();
			app.strokeWeight(6);
			app.pushMatrix();
			Vec trans = d.getTranslationInDomain(d.mask, 0, 0);
			app.translate((float) trans.x, (float) trans.y);
			render.drawBoundary(d.mask.g);
			app.popMatrix();
			app.popStyle();
		}
	}

	public void drawLayout(Layout layout) {
		if (layout.result == null)
			return;
		for (int[] p : layout.result)
			drawPlacement(layout, p);
	}

	public void drawLayoutPostprocessing(Layout layout) {
		if (layout.resultGeometries == null)
			return;
		for (Geometry geo : layout.resultGeometries)
			render.drawBoundary(geo);
	}

	public void drawPlacement(Layout layout, int[] p) {
		DiscreteGeometry geo = layout.templates[p[0]].discreteShape[p[1]];
		app.pushMatrix();
		Vec trans = layout.domain.getTranslationInDomain(geo, p[2], p[3]);

		app.translate((float) trans.x, (float) trans.y);
		render.drawBoundary(geo.g.getBoundary());
		app.popMatrix();

		app.pushStyle();
		int[][] cells = geo.cells;

		for (int[] cell : cells) {
			int a = cell[0] + p[2];
			int b = cell[1] + p[3];
			app.fill(0, (float) geo.maskFloat[cell[0]][cell[1]] * 150);
			app.rect(a * (float) layout.domain.cellSize, b * (float) layout.domain.cellSize, (float) layout.domain.cellSize, (float) layout.domain.cellSize);
		}
		app.popStyle();
	}
}
