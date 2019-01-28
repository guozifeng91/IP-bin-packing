package reference.tests;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import layoutIP.Dataset;
import layoutLPRender.MGraphics;
import processing.core.PApplet;
import processing.data.JSONObject;
import reference.pack.M;
import reference.pack.Pack;
import reference.pack.Strip;

/**
 * 
 * @author Hao Hua, Southeast University Nanjing, http://labaaa.org
 *
 * 2D bin packing places a given set of polygons in standard single/multiple rectangular sheet(s), to minimize the use of the sheet(s).
 * This library does not involve other libraries, however, the example uses core.jar (https://processing.org) for a graphical interface.
 * The algorithm is effective when the ratio  (number of polygons / number of the types of polygons) is small.
 * 
 * 
 * 1. Input
 * a. Only simple polygon: no holes, no self-intersection.
 * b. Data structure: point - double[];  polygon - double[][]; all polygons - double[][][]. 
 * One might use other library to convert  other formats (e.g. dxf, obj) of polygon to double[][].
 * c. It's better to represent a polygon with a moderate number of points. 
 * Too many points (e.g. a local detail contains dozens of points) slows down the algorithm. Avoid using too many points for a smooth curve.
 * One might use segment_max_length to create more points on a long edge of a polygon, if the polygon has very few points or the edge is very long.
 * 
 * 
 * 2. Choose an algorithm
 * a. useAbey=true, Jostle heuristics for the 2D-irregular shapes bin packing problems with free rotation, R. P. Abeysooriya 2018
 * rotSteps:  Each polygon is rotated in the layout. Few steps (say 16) -> run fast  & poor result;  many steps (say 48) -> run slow & good results
 * segment_max_length: if (an edge of a polygon > segment_max_length),  breaks it into smaller segments. 
 * large value -> run fast  & poor result; small value -> run slow & good results
 * segment_max_length is related to translation steps.
 * 
 * b. useAbey=false, Waste minimization in irregular stock cutting, D. Dalalah, 2014
 * the rotation/translation steps depend on the polygons.
 * 
 * 
 * 3. Output
 * 
 * 
 * 
 * 
 */
public class Test_Benchmark extends PApplet {
	/**
	 * benchmark parameters:
	 * 
	 * num. s1, s2
	 * 
	 * swim: 25, 0.5, 0
	 * 
	 * fu: 40, 30, 20
	 * 
	 * han: 38, 30, 20
	 * 
	 * mao: 38, 0.7, 0.3
	 */
	private DecimalFormat df=new DecimalFormat("##.###");
	
	//input
	private double[][][] randompolys;  //the input polygons
	private final float WID = 2400;  //width of the standard rectangular sheet
	private final float HEI = 1200;
	private final double margin= 6.0; 
	private final double preferX=0.499; // 0.501 or 1
	
	//parameters
	private final double segment_max_length =50.0; //250,400,800, use to break long edges if necessary, relative to the scale of the polgyons
	private final int rotSteps=360 ; //18,24,36,48, rotation steps
	private ArrayList<Pack> packs=new ArrayList<Pack>();
	private boolean useAbey=true;  
	// true: rotation steps depend on polygons,  R. P. Abeysooriya 2018
	// false: rotation steps are a prior,  D. Dalalah, 2014
	
	//output
	private int[] result_pack_id;  // result_pack_id[9]=2 means the 9th polygon is on the 2nd sheet.
	private double[][] result_cos_sin; // result_cos_sin[9]={0.5, 0.866} means the 9th polygon is rotated 60 degree w.r.t its reference point
	private double[][] result_position; // result_cos_sin[9] denotes the x,y-coordinate of the 9th polygon w.r.t its reference point

	public void setup() {
		size(1300, 800);
		
		int seed = 4346;
		Random ran = new Random(seed);

		int polyNum = 100;
		randompolys = Dataset.getRandomPolygons(ran, Dataset.poly(), polyNum, 10,0, false);	
		
//		int polyNum = 38;
//		randompolys = Dataset.getRandomPolygons(ran, Dataset.mao(), polyNum, 0.7, 0.3, false);	
		
//		int polyNum = 38;
//		randompolys = Dataset.getRandomPolygons(ran, Dataset.han(), polyNum, 30, 20, false);		
		
//		int polyNum = 40;
//		randompolys = Dataset.getRandomPolygons(ran, Dataset.fu(), polyNum, 30, 20, false);
		
//		int polyNum = 25;
//		randompolys = Dataset.getRandomPolygons(ran, Dataset.swim(), polyNum, 0.5, 0, false);

		Double segment_len = segment_max_length;
		Pack pack = new Pack(randompolys, margin, segment_len, rotSteps, WID, HEI, preferX);
		pack.packOneSheet(useAbey);
		packs.add(pack);

		for (int i = 0; i < 100; i++) { // packing one sheet after another, 100 is estimated
			int size = packs.size();
			if (packs.get(size - 1).isEmpty()) {
				println(size + " sheets");
				break;
			}
			pack = packs.get(size - 1).clone();
			pack.packOneSheet(useAbey);
			packs.add(pack);
		}
		report();
		
		// export json for rendering
		String path = "C:\\Users\\guo zifeng\\Desktop\\paper revision\\graphics\\";
		String saveto = "tiny_Abey";

		JSONObject json = MGraphics.writeJSON(packs, WID, HEI, 3, WID + 100, HEI + 100);
		json.save(new File(path + saveto + ".json"), "");
	}

	private void report() {
		result_pack_id = new int[randompolys.length];
		result_cos_sin = new double[randompolys.length][];
		result_position = new double[randompolys.length][];
		for (int i = 0; i < packs.size(); i++) {
			Pack pack = packs.get(i);
			for (Strip strip : pack.fixs) {
				result_pack_id[strip.id] = i;
				result_cos_sin[strip.id] = strip.trigo;
				result_position[strip.id] = strip.position;
			}
		}
	}

	public void draw() {
		background(255);
		smooth();
		translate(40, 10);
		float sc = 0.15f;

		//display method 1
//		for (int i = 0; i < randompolys.length; i++) {
//			int pack_id = result_pack_id[i];
//			pushMatrix();
//			translate((pack_id % 3) * 380, (pack_id / 3) * 200); 
//			
//			noFill();
//			rect(0, 0, sc * WID, sc * HEI);
//			fill(0, 255, 0);
//			double[][] poly = randompolys[i];
//			poly = M.rotate(result_cos_sin[i], poly);
//			poly = M.move(result_position[i], poly);
//			draw(poly, sc);
//			
//			popMatrix();
//		}
		//display method 2
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 3; j++) {
				int id = i * 3 + j;
				if (id >= packs.size())
					return;
				Pack pack = packs.get(id);
				pushMatrix();
				translate(j * 380, i * 200); // (j * 270, i * 150
				noFill();
				rect(0, 0, sc * WID, sc * HEI);
				fill(0, 255, 0);
				for (Strip strip : pack.fixs) 
					draw(strip.inps, sc);
				popMatrix();
			}
		}
	}

	private void draw(double[][] ps, float sc) {
		beginShape();
		for (double[] p : ps)
			vertex((float) p[0] * sc, (float) p[1] * sc);
		endShape(CLOSE);
	}

}
