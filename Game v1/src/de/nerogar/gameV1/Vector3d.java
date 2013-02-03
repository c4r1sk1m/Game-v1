package de.nerogar.gameV1;

public class Vector3d {

	private double x = 0;
	private double y = 0;
	private double z = 0;
	private double value = 0;
	private boolean isValueDirty = true;
	private double squaredValue;
	private boolean isSquaredValueDirty;
	
	public Vector3d() {
		this(0,0,0);
	}
	
	public Vector3d(double xn, double yn, double zn) {
		set(xn, yn, zn);
	}
	
	public Vector3d(Vector3d v) {
		set(v);
	}
	
	public double getX() { return this.x; }
	public double getY() { return this.y; }
	public double getZ() { return this.z; }

	public float getXf() { return (float) this.getX(); }
	public float getYf() { return (float) this.getY(); }
	public float getZf() { return (float) this.getZ(); }
	
	public boolean equals(Vector3d v) {
		if (v.getX() == x &&
			v.getY() == y &&
			v.getZ() == z) return true;
		return false;
	}
	
	public boolean isNull() {
		if (x==0 && y==0 && z==0) return true;
		return false;
	}
	
	public void setX(double x) {
		this.x = x;
		setValueDirty(true);
	}
	
	public void setY(double y) {
		this.y = y;
		setValueDirty(true);
	}
	
	public void setZ(double z) {
		this.z = z;
		setValueDirty(true);
	}
	
	public Vector3d add(Vector3d v2) {
		setX(getX()+v2.getX());
		setY(getY()+v2.getY());
		setZ(getZ()+v2.getZ());
		return this;
	}
	
	public static Vector3d add(Vector3d v1, Vector3d v2) {
		return v1.clone().add(v2);
	}
	
	public Vector3d subtract(Vector3d v2) {
		return add(Vector3d.invert(v2));
	}
	
	public static Vector3d subtract(Vector3d v1, Vector3d v2) {
		return v1.clone().subtract(v2);
	}
	
	public void addX(double n) {
		setX(getX()+n);
	}
	public void addY(double n) {
		setY(getY()+n);
	}
	public void addZ(double n) {
		setZ(getZ()+n);
	}
	
	public void multiplyX(double n) {
		setX(getX()*n);
	}
	public void multiplyY(double n) {
		setY(getY()*n);
	}
	public void multiplyZ(double n) {
		setZ(getZ()*n);
	}
	
	public Vector3d invert() {
		setX(getX()*-1);
		setY(getY()*-1);
		setZ(getZ()*-1);
		return this;
	}
	
	public static Vector3d invert(Vector3d v) {
		return v.clone().invert();
	}

	public void set(Vector3d v2) {
		setX(v2.getX());
		setY(v2.getY());
		setZ(v2.getZ());
	}

	public void set(double nx, double ny, double nz) {
		setX(nx);
		setY(ny);
		setZ(nz);
	}

	public Vector3d multiply(Vector3d v2) {
		setX(getX()*v2.getX());
		setY(getY()*v2.getY());
		setZ(getZ()*v2.getZ());
		return this;
	}
	
	public static Vector3d multiply(Vector3d v1, Vector3d v2) {
		return v1.clone().multiply(v2);
	}

	public Vector3d multiply(double n) {
		setX(getX()*n);
		setY(getY()*n);
		setZ(getZ()*n);
		return this;
	}
	
	public static Vector3d multiply(Vector3d v, double n) {
		return v.clone().multiply(n);
	}
	
	public double getValue() {
		if (isValueDirty()) recalculateValue();
		return this.value;
	}
	
	public double getSquaredValue() {
		if (isSquaredValueDirty()) recalculateSquaredValue();
		return this.squaredValue;
	}
	
	private boolean isSquaredValueDirty() {
		return this.isSquaredValueDirty;
	}

	private void recalculateValue() {
		if (isSquaredValueDirty()) recalculateSquaredValue();
		setValue(Math.sqrt(this.squaredValue));
		this.setValueDirty(false);
	}
	
	private void recalculateSquaredValue() {
		setSquaredValue(x*x+y*y+z*z);
		this.setSquaredValueDirty(false);
	}

	private void setSquaredValueDirty(boolean isSquaredValueDirty) {
		this.isSquaredValueDirty = isSquaredValueDirty;
	}

	private void setSquaredValue(double squaredValue) {
		this.squaredValue = squaredValue;
	}

	public Vector3d crossProduct(Vector3d v1) {
		// Kreuzprodukt
		double newX = getY()*v1.getZ() - getZ()*v1.getY();
		double newY = getZ()*v1.getX() - getX()*v1.getZ();
		double newZ = getX()*v1.getY() - getY()*v1.getX();
		setX(newX);
		setY(newY);
		setZ(newZ);
		return this;
	}
	
	public static Vector3d crossProduct(Vector3d v1, Vector3d v2) {
		return v1.clone().crossProduct(v2);
	}
	
	public double dotProduct(Vector3d v) {
		// Skalarprodukt
		return getX()*v.getX() + getY()*v.getY() + getZ()*v.getZ();
	}
	
	public static double dotProduct(Vector3d v1, Vector3d v2) {
		return v1.getX()*v2.getX() + v1.getY()*v2.getY() + v1.getZ()*v2.getZ();
	}
	
	public void setValue(double value) {
		this.value = value;
	}

	public boolean isValueDirty() {
		return isValueDirty;
	}

	public void setValueDirty(boolean isValueDirty) {
		this.isValueDirty = isValueDirty;
		if (isValueDirty == true) setSquaredValueDirty(true);
	}
	
	public Vector3d normalize() {
		this.x /= this.value;
		this.y /= this.value;
		this.z /= this.value;
		setValueDirty(true);
		return this;
	}
	
	public Vector3d clone() {
		Vector3d v2 = new Vector3d(this);
		v2.setValueDirty(this.isValueDirty());
		v2.setSquaredValueDirty(this.isSquaredValueDirty());
		v2.setValue(this.getValue());
		v2.setSquaredValue(this.getSquaredValue());
		return v2;
	}
	
	public Vector3d add(double n) {
		addX(n);
		addY(n);
		addZ(n);
		return this;
	}

	public static Vector3d add(Vector3d v1, double n) {
		return v1.clone().add(n);
	}
	
	public boolean isParallelTo(Vector3d v) {
		if (this.isNull() || v.isNull()) return false;
		if (Vector3d.crossProduct(this, v).isNull()) return true;
		return false;
	}
	
	public static boolean isParallelTo(Vector3d v1, Vector3d v2) {
		return v1.clone().isParallelTo(v2);
	}
	
	public String toString() {
		return "("+Double.toString(this.getX())+","+Double.toString(this.getY())+","+Double.toString(this.getZ())+")";
	}

}
