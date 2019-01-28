package layoutIP;

import java.util.ArrayList;

import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;
import gurobi.GRB.DoubleParam;
import gurobi.GRB.IntParam;

public class GurobiSolver implements IPSolver {
	int seed, time;

	public GurobiSolver(int s, int t) {
		seed = s;
		time = t;
	}

	@Override
	public Integer[] solve(Layout p) {
		boolean[] result = null;
		try {
			result = solve_(p);
		} catch (GRBException e) {
			e.printStackTrace();
			return null;
		}

		ArrayList<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < result.length; i++) {
			if (result[i]) {
				list.add(Integer.valueOf(i));
			}
		}

		return list.toArray(new Integer[list.size()]);
	}

	private boolean[] solve_(Layout p) throws GRBException {
		GRBEnv env = initEnvironment();
		GRBModel model = new GRBModel(env);

		int pNum = p.placements.size();
		/*
		 * add variables
		 */
		GRBVar[] vars = addVar(model, GRB.BINARY, pNum, "placement");

		model.update();

		/*
		 * set objective
		 */

		GRBLinExpr expr = new GRBLinExpr();

		addObjective(expr, vars, p);
		model.setObjective(expr, GRB.MAXIMIZE);

		/*
		 * set constraints
		 */
		addFaceConstraints(model, vars, p);
		addTemplateConstraints(model, vars, p);

		/*
		 * optimize
		 */
		model.optimize();

		boolean[] result = getResult(vars);

		model.dispose();
		env.dispose();

		return result;
	}

	/**
	 * 
	 * @param vars
	 * @return
	 * @throws GRBException
	 */
	private boolean[] getResult(GRBVar[] vars) throws GRBException {
		int len = vars.length;
		boolean[] out = new boolean[len];
		for (int i = 0; i < len; i++) {
			double r = vars[i].get(GRB.DoubleAttr.X);
			double abs = Math.abs(r);
			/*
			 * may output 1.0000000X or 0.000000000X such thing
			 */
			out[i] = !(abs <= 1E-6);
		}
		return out;
	}

	/**
	 * For each <b>t</b> &isin; <b>T</b>, &sum;<b>P<sub>t</sub> &le;
	 * n<sub>t</sub></b>
	 * 
	 * @param model
	 * @param vars
	 * @param p
	 * @throws GRBException
	 */
	private void addTemplateConstraints(GRBModel model, GRBVar[] vars, Layout p) throws GRBException {
		int num = p.templates.length;
		for (int i = 0; i < num; i++) {
			GRBLinExpr expr = new GRBLinExpr();
			for (Integer index : p.placements_template[i]) {
				expr.addTerm(1, vars[index.intValue()]);
			}
			model.addConstr(expr, GRB.LESS_EQUAL, p.templates[i].instNum, "template" + i);
		}

	}

	/**
	 * For each <b>d</b> &isin; <b>D</b>, &sum;<b>P<sub>d</sub> &le; 1</b> <br>
	 * 
	 * @param model
	 * @param vars
	 * @param p
	 * @throws GRBException
	 */
	private void addFaceConstraints(GRBModel model, GRBVar[] vars, Layout p) throws GRBException {
		int xNum = p.domain.xNum;
		int yNum = p.domain.yNum;

		for (int i = 0; i < xNum; i++) {
			for (int j = 0; j < yNum; j++) {
				GRBLinExpr expr = new GRBLinExpr();

				for (Integer index : p.placements_cell[i][j]) {
					float weight;
					if (p.domain.useFloatMask) {
						int[] placement = p.placements.get(index);
						int px = i - placement[2];
						int py = j - placement[3];
						weight = (float) p.templates[placement[0]].discreteShape[placement[1]].maskFloat[px][py];
						weight = (float) Math.pow(weight, p.domain.distPowerRatio);
					} else {
						weight = 1;
					}
					expr.addTerm(weight, vars[index.intValue()]);
				}

				model.addConstr(expr, GRB.LESS_EQUAL, 1, "face" + i + "-" + j);
			}
		}
	}

	/**
	 * 
	 * @param expr
	 * @param vars
	 * @param layout
	 */
	private void addObjective(GRBLinExpr expr, GRBVar[] vars, Layout layout) {
		for (int i = 0; i < vars.length; i++) {
			int[] placement = layout.placements.get(i);
			expr.addTerm(layout.templates[placement[0]].weight, vars[i]);
		}
	}

	/**
	 * @param model
	 * @param num
	 * @param type
	 * @param name
	 * @return
	 * @throws GRBException
	 */
	private GRBVar[] addVar(GRBModel model, char type, int num, String name) throws GRBException {
		System.out.println("Variable \"" + name + "\" " + num);
		GRBVar[] vars = new GRBVar[num];
		for (int i = 0; i < num; i++) {
			vars[i] = model.addVar(0, 1, 0, type, name + i);
		}
		return vars;
	}

	private GRBEnv initEnvironment() throws GRBException {
		GRBEnv env = new GRBEnv();

		int processors = Runtime.getRuntime().availableProcessors();
		System.out.println("Avaliable Processors " + processors);
		env.set(IntParam.Threads, processors);
		if (time > 0) {
			env.set(DoubleParam.TimeLimit, time);
		}

		env.set(IntParam.Seed, seed);
		return env;
	}
}
