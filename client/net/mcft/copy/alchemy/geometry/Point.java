package net.mcft.copy.alchemy.geometry;

import java.util.Locale;

public class Point {

	public static final Point zero = new Point(0, 0);
	public static final Point one  = new Point(1, 1);
	
	public final double x, y;
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}

	public Point add(double x, double y) {
		return new Point(this.x + y, this.y + y);
	}
	public Point add(Point p) { return add(p.x, p.y); }

	public Point sub(double x, double y) {
		return new Point(this.x - y, this.y - y);
	}
	public Point sub(Point p) { return sub(p.x, p.y); }
	
	public Point mult(double xFactor, double yFactor) {
		return new Point(this.x * xFactor, this.y * yFactor);
	}
	public Point mult(double factor) { return mult(factor, factor); }
	
	public double distanceTo(double x, double y) {
		return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
	}
	public double distanceTo(Point p) { return distanceTo(p.x, p.y); }
	
	/* Returns angle to point in radians. */
	public double angleTo(double x, double y) {
		return Math.atan2(y - this.y, x - this.x);
	}
	/* Returns angle to point in radians. */
	public double angleTo(Point p) { return angleTo(p.x, p.y); }

	/* Returns difference between two angles in radians, from -π to +π. */
	public static double angleDifference(double angle1, double angle2) {
		return (((angle1 - angle2) % (Math.PI * 2) + Math.PI * 3) % (Math.PI * 2) - Math.PI);
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "Point [ %g,%g ]", x, y);
	}

}
