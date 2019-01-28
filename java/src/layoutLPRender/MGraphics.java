package layoutLPRender;

import java.util.ArrayList;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import gzf.Vec;
import layoutIP.DiscreteGeometry;
import layoutIP.Domain;
import layoutIP.Layout;
import layoutIP.Template;
import layoutIP.util.JTSConverter;
import processing.data.JSONArray;
import processing.data.JSONObject;
import reference.pack.Pack;
import reference.pack.Strip;

/**
 * convert the result to a json object for rendering in Mathematica
 * 
 * @author guozifeng
 *
 */
public class MGraphics {
	static void applyTranslate(Vec[] geo, Vec translate) {
		for (Vec v : geo) {
			v.add(translate);
		}
	}

	static JSONArray tojson(Vec[] poly) {
		JSONArray array = new JSONArray();
		for (int i = 0; i < poly.length; i++) {
			JSONArray coord = new JSONArray();
			coord.append(poly[i].x);
			coord.append(poly[i].y);
			array.append(coord);
		}
		return array;
	}

	static JSONArray tojson(int id, Vec[] poly) {
		JSONArray array = new JSONArray();
		array.append(id);
		array.append(tojson(poly));
		return array;
	}

	static JSONArray tojson(double weight, Vec[] poly) {
		JSONArray array = new JSONArray();
		array.append(weight);
		array.append(tojson(poly));
		return array;
	}

	static JSONArray tojson(double[][] poly) {
		JSONArray array = new JSONArray();
		for (int i = 0; i < poly.length; i++) {
			JSONArray coord = new JSONArray();
			coord.append(poly[i][0]);
			coord.append(poly[i][1]);
			array.append(coord);
		}
		return array;
	}

	static JSONArray tojson(int id, double[][] poly) {
		JSONArray array = new JSONArray();
		array.append(id);
		array.append(tojson(poly));
		return array;
	}

	public static JSONObject writeJSON(Template t, int xNum) {
		JSONObject jsonBuf = getJSONTemplate();
		Domain domain = t.domain;
		double tx = 0, ty = 0;
		int num = 0;

		int step = 0;
		for (DiscreteGeometry geo : t.discreteShape) {
			step = Math.max(step, Math.max(geo.xNum, geo.yNum));
		}
		step += 1;

		for (DiscreteGeometry geo : t.discreteShape) {
			num++;
			Vec trans = domain.getTranslationInDomain(geo, 0, 0);
			trans.add(tx, ty, 0);

			// geometry
			Vec[] outer = JTSConverter.getPolygonOuterBoundary(geo.g);
			applyTranslate(outer, trans);
			jsonBuf.getJSONArray("shape_outer").append(tojson(outer));

			Vec[][] inner = JTSConverter.getPolygonInnerBoundary(geo.g);
			if (inner.length > 0) {
				for (Vec[] inner_ : inner) {
					applyTranslate(inner_, trans);
					jsonBuf.getJSONArray("shape_inner").append(tojson(inner_));
				}
			}

			// cells
			int[][] cells = geo.cells;
			for (int[] cell : cells) {
				int a = cell[0];
				int b = cell[1];

				Vec[] rect = new Vec[] { new Vec(a * domain.cellSize + tx, b * domain.cellSize + ty),

						new Vec(a * domain.cellSize + domain.cellSize + tx, b * domain.cellSize + ty),

						new Vec(a * domain.cellSize + domain.cellSize + tx, b * domain.cellSize + domain.cellSize + ty),

						new Vec(a * domain.cellSize + tx, b * domain.cellSize + domain.cellSize + ty) };

				jsonBuf.getJSONArray("cell").append(tojson(geo.maskFloat[a][b], rect));
			}

			if (num < xNum) {
				tx += step * domain.cellSize;
			} else {
				tx = 0;
				num = 0;
				ty += step * domain.cellSize;
			}
		}
		
		return jsonBuf;
	}

	public static void writeJSON(JSONObject jsonBuf, Layout layout, double tx, double ty) {
		Domain domain = layout.domain;
		Template[] templates = layout.templates;
		int[][] result = layout.result;

		// domain
		double w = domain.cellSize * domain.xNum;
		double h = domain.cellSize * domain.yNum;

		Vec[] rectD = new Vec[] { new Vec(tx, ty), new Vec(tx + w, ty), new Vec(tx + w, ty + h), new Vec(tx, ty + h) };

		// boundary style
		jsonBuf.getJSONArray("domain_bound").append(tojson(rectD));

		if (domain.mask != null) {
			Vec trans = domain.getTranslationInDomain(domain.mask, 0, 0).add(tx, ty, 0);

			Vec[] outer = JTSConverter.getPolygonOuterBoundary(domain.mask.g);
			applyTranslate(outer, trans);
			jsonBuf.getJSONArray("domain_outer").append(tojson(outer));

			Vec[][] inner = JTSConverter.getPolygonInnerBoundary(domain.mask.g);
			for (Vec[] inner_ : inner) {
				applyTranslate(inner_, trans);
				jsonBuf.getJSONArray("domain_inner").append(tojson(inner_));
			}
		}

		// placements (exporting holes and texts are not implemented yet)
		for (int[] p : result) {
			DiscreteGeometry geo = templates[p[0]].discreteShape[p[1]];
			// cells
			int[][] cells = geo.cells;
			for (int[] cell : cells) {
				int a = cell[0] + p[2];
				int b = cell[1] + p[3];

				Vec[] rect = new Vec[] { new Vec(a * domain.cellSize + tx, b * domain.cellSize + ty),

						new Vec(a * domain.cellSize + domain.cellSize + tx, b * domain.cellSize + ty),

						new Vec(a * domain.cellSize + domain.cellSize + tx, b * domain.cellSize + domain.cellSize + ty),

						new Vec(a * domain.cellSize + tx, b * domain.cellSize + domain.cellSize + ty) };

				jsonBuf.getJSONArray("cell").append(tojson(p[0], rect));
			}

			Vec trans = domain.getTranslationInDomain(geo, p[2], p[3]);
			trans.add(tx, ty, 0);

			// geometry
			Vec[] outer = JTSConverter.getPolygonOuterBoundary(geo.g);
			applyTranslate(outer, trans);
			jsonBuf.getJSONArray("shape_outer").append(tojson(p[0], outer));

			Vec[][] inner = JTSConverter.getPolygonInnerBoundary(geo.g);
			if (inner.length > 0) {
				for (Vec[] inner_ : inner) {
					applyTranslate(inner_, trans);
					jsonBuf.getJSONArray("shape_inner").append(tojson(p[0], inner_));
				}
			}
		}

		// post processed

		Geometry[] resultGeometries = layout.resultGeometries;
		if (resultGeometries != null) {
			for (int i = 0; i < resultGeometries.length; i++) {
				Vec[] outer = JTSConverter.getPolygonOuterBoundary(resultGeometries[i]);
				applyTranslate(outer, new Vec(tx, ty));
				jsonBuf.getJSONArray("postprocess_outer").append(tojson(result[i][0], outer));

				Vec[][] inner = JTSConverter.getPolygonInnerBoundary(resultGeometries[i]);
				for (Vec[] inner_ : inner) {
					applyTranslate(inner_, new Vec(tx, ty));
					jsonBuf.getJSONArray("postprocess_inner").append(tojson(result[i][0], inner_));
				}
			}
		}
	}

	private static JSONObject getJSONTemplate() {
		JSONObject json = new JSONObject();
		json.setJSONArray("domain_bound", new JSONArray());
		json.setJSONArray("domain_outer", new JSONArray());
		json.setJSONArray("domain_inner", new JSONArray());
		json.setJSONArray("shape_outer", new JSONArray());
		json.setJSONArray("shape_inner", new JSONArray());
		json.setJSONArray("cell", new JSONArray());
		json.setJSONArray("postprocess_inner", new JSONArray());
		json.setJSONArray("postprocess_outer", new JSONArray());

		return json;
	}

	public static JSONObject writeJSON(ArrayList<Layout> layouts, int xNum, double tx, double ty) {
		JSONObject json = getJSONTemplate();
		for (int i = 0; i < layouts.size(); i++) {
			writeJSON(json, layouts.get(i), (i % xNum) * tx, -(i / xNum) * ty);
		}
		return json;
	}

	public static void writeJSON(JSONObject jsonBuf, Pack pack, double w, double h, double tx, double ty) {
		Vec[] rectD = new Vec[] { new Vec(tx, ty), new Vec(tx + w, ty), new Vec(tx + w, ty + h), new Vec(tx, ty + h) };
		// boundary
		jsonBuf.getJSONArray("domain_bound").append(tojson(rectD));

		for (Strip strip : pack.fixs) {
			double[][] coord = strip.inps.clone();
			for (int i = 0; i < coord.length; i++) {
				coord[i] = strip.inps[i].clone();
				coord[i][0] += tx;
				coord[i][1] += ty;
			}
			jsonBuf.getJSONArray("shape_outer").append(tojson(strip.id, coord));
			jsonBuf.getJSONArray("postprocess_outer").append(tojson(strip.id, coord));
		}
	}

	public static JSONObject writeJSON(ArrayList<Pack> packs, double w, double h, int xNum, double tx, double ty) {
		JSONObject json = getJSONTemplate();
		for (int i = 0; i < packs.size(); i++) {
			writeJSON(json, packs.get(i), w, h, (i % xNum) * tx, -(i / xNum) * ty);
		}
		return json;
	}

	public static JSONObject writeJSON(ArrayList<Polygon> polygons) {
		JSONObject json = getJSONTemplate();
		for (int i = 0; i < polygons.size(); i++) {
			Vec[] outer = JTSConverter.getPolygonOuterBoundary(polygons.get(i));
			json.getJSONArray("shape_outer").append(tojson(i, outer));
			json.getJSONArray("postprocess_outer").append(tojson(i, outer));

			Vec[][] inner = JTSConverter.getPolygonInnerBoundary(polygons.get(i));
			for (Vec[] inner_ : inner) {
				json.getJSONArray("shape_inner").append(tojson(i, inner_));
				json.getJSONArray("postprocess_inner").append(tojson(i, inner_));
			}
		}
		return json;
	}
}
