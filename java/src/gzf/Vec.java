package gzf;

/**
 * 3d vector from igeo
 * <p>
 * 
 * too basic to put it in any sub-package
 * 
 * @author guozifeng
 * 
 */
public class Vec {
	public final static Vec origin = new Vec(0, 0, 0);
	public final static Vec xaxis = new Vec(1, 0, 0);
	public final static Vec yaxis = new Vec(0, 1, 0);
	public final static Vec zaxis = new Vec(0, 0, 1);
	public double x, y, z;

	public Vec() {
	}

	public Vec(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vec(double x, double y) {
		this.x = x;
		this.y = y;
		z = 0;
	}

	public Vec(Vec v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	public double x() {
		return x;
	}

	public double y() {
		return y;
	}

	public double z() {
		return z;
	}

	/** setting x component */
	public Vec x(double vx) {
		x = vx;
		return this;
	}

	/** setting y component */
	public Vec y(double vy) {
		y = vy;
		return this;
	}

	/** setting z component */
	public Vec z(double vz) {
		z = vz;
		return this;
	}

	public Vec get() {
		return this;
	}

	// public Vec get(){ return new Vec(x,y,z); }

	public Vec dup() {
		return new Vec(x, y, z);
	}

	public Vec set(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		return this;
	}

	public Vec set(Vec v) {
		x = v.x;
		y = v.y;
		z = v.z;
		return this;
	}

	public Vec add(double x, double y, double z) {
		this.x += x;
		this.y += y;
		this.z += z;
		return this;
	}

	public Vec add(Vec v) {
		x += v.x;
		y += v.y;
		z += v.z;
		return this;
	}

	public Vec sub(double x, double y, double z) {
		this.x -= x;
		this.y -= y;
		this.z -= z;
		return this;
	}

	public Vec sub(Vec v) {
		x -= v.x;
		y -= v.y;
		z -= v.z;
		return this;
	}

	public Vec mul(double v) {
		x *= v;
		y *= v;
		z *= v;
		return this;
	}

	public Vec div(double v) {
		x /= v;
		y /= v;
		z /= v;
		return this;
	}

	public Vec neg() {
		x = -x;
		y = -y;
		z = -z;
		return this;
	}

	/** alias of neg() */
	public Vec rev() {
		return neg();
	}

	/** alias of neg() */
	public Vec flip() {
		return neg();
	}

	/** setting all zero */
	public Vec zero() {
		x = 0;
		y = 0;
		z = 0;
		return this;
	}

	/** scale add */
	public Vec add(Vec v, double f) {
		x += f * v.x;
		y += f * v.y;
		z += f * v.z;
		return this;
	}

	/** scale add; alias of add(Vec,double) */
	public Vec add(double f, Vec v) {
		return add(v, f);
	}

	/** dot product in double */
	public double dot(Vec v) {
		return x * v.x + y * v.y + z * v.z;
	}

	/** dot product in double */
	public double dot(double vx, double vy, double vz) {
		return x * vx + y * vy + z * vz;
	}

	/**
	 * cross product. returning a new instance, not changing own content!
	 * 2011/08/03
	 */
	public Vec cross(Vec v) {
		// double xt = y*v.z - z*v.y;
		// double yt = z*v.x - x*v.z;
		// double zt = x*v.y - y*v.x;
		// x=xt; y=yt; z=zt; return this;
		return new Vec(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
	}

	/** cross product. */
	public Vec cross(double vx, double vy, double vz) {
		return new Vec(y * vz - z * vy, z * vx - x * vz, x * vy - y * vx);
	}

	/** cross product, changing its values by itself. no new instance created. */
	public Vec icross(Vec v) {
		double xt = y * v.z - z * v.y;
		double yt = z * v.x - x * v.z;
		double zt = x * v.y - y * v.x;
		x = xt;
		y = yt;
		z = zt;
		return this;
	}

	/** cross product, changing its values by itself. no new instance created. */
	public Vec icross(double vx, double vy, double vz) {
		double xt = y * vz - z * vy;
		double yt = z * vx - x * vz;
		double zt = x * vy - y * vx;
		x = xt;
		y = yt;
		z = zt;
		return this;
	}

	/** get length of the vector */
	public double len() {
		return Math.sqrt(x * x + y * y + z * z);
	}

	/** get squared length of the vector */
	public double len2() {
		return x * x + y * y + z * z;
	}

	/** set length of the vector */
	public Vec len(double l) {
		l /= len();
		x *= l;
		y *= l;
		z *= l;
		return this;
	}

	/** unitize the vector */
	public Vec unit() {
		double l = len();
		// if(l==0){ IOut.err("vector length is zero"); return this; } // added
		// 20111002 // removed 20111003
		if (l != 0) { // add my guozifeng
			x /= l;
			y /= l;
			z /= l;
		}
		return this;
	}

	/** get distance between two vectors */
	public double dist(Vec v) {
		return Math.sqrt((x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) + (z - v.z) * (z - v.z));
	}

	/** get distance between two vectors */
	public double dist(double vx, double vy, double vz) {
		return Math.sqrt((x - vx) * (x - vx) + (y - vy) * (y - vy) + (z - vz) * (z - vz));
	}

	/** get squared distance between two vectors */
	public double dist2(Vec v) {
		return (x - v.x) * (x - v.x) + (y - v.y) * (y - v.y) + (z - v.z) * (z - v.z);
	}

	/** get squared distance between two vectors */
	public double dist2(double vx, double vy, double vz) {
		return (x - vx) * (x - vx) + (y - vy) * (y - vy) + (z - vz) * (z - vz);
	}

	/** check if 2 vectors are same by distance with tolerace */
	public boolean eq(Vec v, double tolerance) {
		return dist2(v) <= tolerance * tolerance;
	}

	/** check if 2 vectors are same by distance with tolerace */
	public boolean eq(double vx, double vy, double vz, double tolerance) {
		return dist2(vx, vy, vz) <= tolerance * tolerance;
	}

	public boolean eqX(Vec v, double tolerance) {
		return Math.abs(x - v.x) <= tolerance;
	}

	public boolean eqY(Vec v, double tolerance) {
		return Math.abs(y - v.y) <= tolerance;
	}

	public boolean eqZ(Vec v, double tolerance) {
		return Math.abs(z - v.z) <= tolerance;
	}

	public boolean eqX(double vx, double tolerance) {
		return Math.abs(x - vx) <= tolerance;
	}

	public boolean eqY(double vy, double tolerance) {
		return Math.abs(y - vy) <= tolerance;
	}

	public boolean eqZ(double vz, double tolerance) {
		return Math.abs(z - vz) <= tolerance;
	}

	/** angle in radian, ranging from 0 to Pi */
	public double angle(Vec v) {
		double len1 = len();
		if (len1 == 0)
			return 0;
		double len2 = v.len();
		if (len2 == 0)
			return 0;
		double cos = dot(v) / (len1 * len2);
		if (cos > 1.)
			cos = 1;
		else if (cos < -1.)
			cos = -1; // in case of rounding error
		return Math.acos(cos);
	}

	/** angle in radian, ranging from 0 to Pi */
	public double angle(double vx, double vy, double vz) {
		double len1 = len();
		if (len1 == 0)
			return 0;
		double len2 = Math.sqrt(vx * vx + vy * vy + vz * vz);
		if (len2 == 0)
			return 0;
		double cos = dot(vx, vy, vz) / (len1 * len2);
		if (cos > 1.)
			cos = 1;
		else if (cos < -1.)
			cos = -1; // in case of rounding error
		return Math.acos(cos);
	}

	/**
	 * angle in radian, with reference axis to decide which is negative
	 * direction, ranging from -Pi to Pi angle is not measured on the plane of
	 * axis. axis just define if it's positive angle or negative angle
	 */
	public double angle(Vec v, Vec axis) {
		// double dot = x*v.x+y*v.y+z*v.z;
		double len1 = len();
		if (len1 == 0)
			return 0;
		double len2 = v.len();
		if (len2 == 0)
			return 0;
		double cos = dot(v) / (len1 * len2);
		// Vec cross = dup().cross(v);
		Vec cross = cross(v);
		if (cos > 1.)
			cos = 1;
		else if (cos < -1.)
			cos = -1; // in case of rounding error
		double angle = Math.acos(cos);
		if (cross.dot(axis) < 0)
			return -angle;
		return angle;
	}

	/**
	 * angle in radian, with reference axis to decide which is negative
	 * direction, ranging from -Pi to Pi angle is not measured on the plane of
	 * axis. axis just define if it's positive angle or negative angle
	 */
	public double angle(double vx, double vy, double vz, double axisX, double axisY, double axisZ) {
		double len1 = len();
		if (len1 == 0)
			return 0;
		double len2 = Math.sqrt(vx * vx + vy * vy + vz * vz);
		if (len2 == 0)
			return 0;
		double cos = dot(vx, vy, vz) / (len1 * len2);
		Vec cross = cross(vx, vy, vz);
		if (cos > 1.)
			cos = 1;
		else if (cos < -1.)
			cos = -1; // in case of rounding error
		double angle = Math.acos(cos);
		if (cross.dot(axisX, axisY, axisZ) < 0)
			return -angle;
		return angle;
	}

	/** rotate the vector around the axis */
	public Vec rot(Vec axis, double angle) {
		if (axis == null)
			return rot(angle); // should have null check?

		double mat[][] = new double[3][3];
		Vec ax = axis.dup().unit();
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double icos = 1 - cos;

		// right-handed coordinates
		mat[0][0] = ax.x * ax.x * icos + cos;
		mat[0][1] = ax.x * ax.y * icos - ax.z * sin;
		mat[0][2] = ax.x * ax.z * icos + ax.y * sin;
		mat[1][0] = ax.y * ax.x * icos + ax.z * sin;
		mat[1][1] = ax.y * ax.y * icos + cos;
		mat[1][2] = ax.y * ax.z * icos - ax.x * sin;
		mat[2][0] = ax.z * ax.x * icos - ax.y * sin;
		mat[2][1] = ax.z * ax.y * icos + ax.x * sin;
		mat[2][2] = ax.z * ax.z * icos + cos;

		// left-handed coordinates
		// mat[0][0] = ax.x*ax.x*icos + cos;
		// mat[0][1] = ax.x*ax.y*icos + ax.z*sin;
		// mat[0][2] = ax.x*ax.z*icos - ax.y*sin;
		// mat[1][0] = ax.y*ax.x*icos - ax.z*sin;
		// mat[1][1] = ax.y*ax.y*icos + cos;
		// mat[1][2] = ax.y*ax.z*icos + ax.x*sin;
		// mat[2][0] = ax.z*ax.x*icos + ax.y*sin;
		// mat[2][1] = ax.z*ax.y*icos - ax.x*sin;
		// mat[2][2] = ax.z*ax.z*icos + cos;

		double xt = x;
		double yt = y;
		x = mat[0][0] * xt + mat[0][1] * yt + mat[0][2] * z;
		y = mat[1][0] * xt + mat[1][1] * yt + mat[1][2] * z;
		z = mat[2][0] * xt + mat[2][1] * yt + mat[2][2] * z;
		return this;
	}

	/** rotate the vector around the axis */
	public Vec rot(double axisX, double axisY, double axisZ, double angle) {
		double mat[][] = new double[3][3];
		double len = Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);
		if (len == 0)
			return this; // do nothing
		axisX /= len;
		axisY /= len;
		axisZ /= len;
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double icos = 1 - cos;

		// right-handed coordinates
		mat[0][0] = axisX * axisX * icos + cos;
		mat[0][1] = axisX * axisY * icos - axisZ * sin;
		mat[0][2] = axisX * axisZ * icos + axisY * sin;
		mat[1][0] = axisY * axisX * icos + axisZ * sin;
		mat[1][1] = axisY * axisY * icos + cos;
		mat[1][2] = axisY * axisZ * icos - axisX * sin;
		mat[2][0] = axisZ * axisX * icos - axisY * sin;
		mat[2][1] = axisZ * axisY * icos + axisX * sin;
		mat[2][2] = axisZ * axisZ * icos + cos;

		// left-handed coordinates
		// mat[0][0] = axisX*axisX*icos + cos;
		// mat[0][1] = axisX*axisY*icos + axisZ*sin;
		// mat[0][2] = axisX*axisZ*icos - axisY*sin;
		// mat[1][0] = axisY*axisX*icos - axisZ*sin;
		// mat[1][1] = axisY*axisY*icos + cos;
		// mat[1][2] = axisY*axisZ*icos + axisX*sin;
		// mat[2][0] = axisZ*axisX*icos + axisY*sin;
		// mat[2][1] = axisZ*axisY*icos - axisX*sin;
		// mat[2][2] = axisZ*axisZ*icos + cos;

		double xt = x;
		double yt = y;
		x = mat[0][0] * xt + mat[0][1] * yt + mat[0][2] * z;
		y = mat[1][0] * xt + mat[1][1] * yt + mat[1][2] * z;
		z = mat[2][0] * xt + mat[2][1] * yt + mat[2][2] * z;
		return this;
	}

	/** rotation on xy-plane */
	public Vec rot(double angle) {
		double sin = Math.sin(angle);
		double cos = Math.cos(angle);
		double xt = x;
		x = cos * xt - sin * y;
		y = sin * xt + cos * y;
		return this;
	}

	public Vec rot(Vec center, Vec axis, double angle) {
		if (center == this)
			return this;
		return sub(center).rot(axis, angle).add(center);
	}

	public Vec rot(double centerX, double centerY, double centerZ, double axisX, double axisY, double axisZ, double angle) {
		return sub(centerX, centerY, centerZ).rot(axisX, axisY, axisZ, angle).add(centerX, centerY, centerZ);
	}

	// test this method later!!!
	/** rotation around axis towards destination direction */
	public Vec rot(Vec axis, Vec destDir) {
		return rot(axis, destDir.cross(axis).angle(cross(axis)));
	}

	/** rotation on xy-plane; alias of rot(double) */
	public Vec rot2(double angle) {
		return rot(angle);
	}

	/** rotation on xy-plane */
	public Vec rot2(Vec center, double angle) {
		if (center == this) {
			return this;
		}
		return sub(center).rot(angle).add(center);
	}

	/** rotation on xy-plane */
	public Vec rot2(double centerX, double centerY, double angle) {
		return sub(centerX, centerY, 0).rot(angle).add(centerX, centerY, 0);
	}

	// test this method later!!!
	/** rotation on xy-plane towards destDir */
	public Vec rot2(Vec destDir) {
		return rot(destDir.cross(zaxis).angle(cross(zaxis)));
	}

	public Vec scale(double f) {
		return mul(f);
	}

	/** scale from the center */
	public Vec scale(Vec center, double f) {
		if (center == this)
			return this;
		return sub(center).scale(f).add(center);
	}

	/** scale from the center */
	public Vec scale(double centerX, double centerY, double centerZ, double f) {
		return sub(centerX, centerY, centerZ).scale(f).add(centerX, centerY, centerZ);
	}

	/** reflect (mirror) 3 dimensionally to the other side of the plane */
	public Vec ref(Vec planeDir) {
		// planeDir = planeDir.dup().unit();
		// return add(planeDir.mul(dot(planeDir)*-2));
		return add(planeDir.dup().mul(dot(planeDir) / planeDir.len2() * -2));
	}

	/** reflect (mirror) 3 dimensionally to the other side of the plane */
	public Vec ref(double planeX, double planeY, double planeZ) {
		double d = dot(planeX, planeY, planeZ) / (planeX * planeX + planeY * planeY + planeZ * planeZ) * -2;
		x += planeX * d;
		y += planeY * d;
		z += planeZ * d;
		return this;
	}

	/**
	 * reflect (mirror) 3 dimensionally to the other side of the plane at the
	 * center
	 */
	public Vec ref(Vec center, Vec planeDir) {
		if (center == this)
			return this;
		return sub(center).ref(planeDir).add(center);
	}

	/**
	 * reflect (mirror) 3 dimensionally to the other side of the plane at the
	 * center
	 */
	public Vec ref(double centerX, double centerY, double centerZ, double planeX, double planeY, double planeZ) {
		return sub(centerX, centerY, centerZ).ref(planeX, planeY, planeZ).add(centerX, centerY, centerZ);
	}

	/** alias of ref */
	public Vec mirror(Vec planeDir) {
		return ref(planeDir);
	}

	/** alias of ref */
	public Vec mirror(double planeX, double planeY, double planeZ) {
		return ref(planeX, planeY, planeZ);
	}

	/** alias of ref */
	public Vec mirror(Vec center, Vec planeDir) {
		return ref(center, planeDir);
	}

	/** alias of ref */
	public Vec mirror(double centerX, double centerY, double centerZ, double planeX, double planeY, double planeZ) {
		return ref(centerX, centerY, centerZ, planeX, planeY, planeZ);
	}

	public String toString() {
		return x + ", " + y + ", " + z;
	}

	public double[] toDoubleArray() {
		return new double[] { x, y, z };
	}

	/**
	 * 生成随机方向的指定长度的向量
	 */
	public static Vec randomVec(double len) {
		double[] cood = new double[3];
		for (int i = 0; i < cood.length; i++) {
			cood[i] = Math.random() - 0.5;
		}
		return new Vec(cood[0], cood[1], cood[2]).unit().mul(len);
	}

	/**
	 * 生成随机方向的指定长度的向量
	 */
	public static Vec randomVec2d(double len) {
		double[] cood = new double[2];
		for (int i = 0; i < cood.length; i++) {
			cood[i] = Math.random() - 0.5;
		}
		return new Vec(cood[0], cood[1], 0).unit().mul(len);
	}

	/**
	 * 随机点
	 */
	public static Vec randomVec(double x0, double x1, double y0, double y1) {
		return randomVec(x0, x1, y0, y1, 0, 0);
	}

	/**
	 * 随机点
	 */
	public static Vec randomVec(double x0, double x1, double y0, double y1, double z0, double z1) {
		return new Vec(Math.random() * (x1 - x0) + x0, Math.random() * (y1 - y0) + y0, Math.random() * (z1 - z0) + z0);
	}

	/**
	 * 三点是否共线的判断
	 */
	public static boolean inSameLine(Vec p1, Vec p2, Vec p3) {
		Vec ptp1 = p3.dup().sub(p1).unit();
		Vec ptp2 = p3.dup().sub(p2).unit();
		return ((ptp1.eq(ptp2, 0.001)) || (ptp1.eq(ptp2.dup().mul(-1), 0.002)));
	}

	/**
	 * p是否介于p1,p2之间
	 */
	public static boolean between(Vec p, Vec p1, Vec p2) {
		return (between(p.x, p1.x, p2.x) && between(p.y, p1.y, p2.y) && between(p.z, p1.z, p2.z));
	}

	/**
	 * 数值a介于bc两者之间
	 */
	private static boolean between(double a, double b, double c) {
		return ((a >= b && a <= c) || (a >= c && a <= b));
	}

	/**
	 * 点到直线的距离
	 */
	public static double distToLine(Vec p, Vec p1, Vec p2) {
		if (inSameLine(p, p1, p2))
			return 0;
		Vec direct = p2.dup().sub(p1);
		Vec ptp = p.dup().sub(p1);
		// 简化运算 direct.unit
		direct.unit();
		return (Math.sqrt(Math.pow(ptp.len(), 2) - Math.pow(direct.dot(ptp), 2)));
	}

	/**
	 * 点到线段的距离
	 */
	public static double distToSegment(Vec p, Vec p1, Vec p2) {
		if (inSameLine(p, p1, p2)) {
			if (between(p, p1, p2)) {
				return 0;
			} else {
				return Math.min(p.dist(p1), p.dist(p2));
			}
		} else {
			Vec direct = p2.dup().sub(p1);
			Vec ptp = p.dup().sub(p1);
			double t = ptp.dot(direct) / Math.pow(direct.len(), 2);
			if (t < 0) {
				return p1.dist(p);
			} else if (t > 1) {
				return p2.dist(p);
			} else {
				return distToLine(p, p1, p2);
			}
		}
	}

	/**
	 * 求中点
	 */
	public static Vec midPt(Vec p1, Vec p2) {
		return new Vec((p1.x + p2.x) / 2, (p1.y + p2.y) / 2, (p1.z + p2.z) / 2);
	}
}
