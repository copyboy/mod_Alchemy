package client.net.mcft.copy.alchemy.geometry;

import java.util.*;

public class BoundingBox {

	public static final BoundingBox zero     = new BoundingBox( 0,  0, 0, 0);
	public static final BoundingBox one      = new BoundingBox( 0,  0, 1, 1);
	public static final BoundingBox identity = new BoundingBox(-1, -1, 1, 1);
	
	public final double minX, minY, maxX, maxY;
	
	public BoundingBox(double minX, double minY, double maxX, double maxY) {
		if (minX > maxX || minY > maxY)
			throw new Error("Min is larger than max.");
		this.minX = minX;
		this.minY = minY;
		this.maxX = maxX;
		this.maxY = maxY;
	}
	public BoundingBox(Point min, Point max) {
		this(min.x, min.y, max.x, max.y);
	}
	
	public static BoundingBox fromPositionAndSize(double x, double y, double width, double height) {
		return new BoundingBox(x, y, x + width, y + height);
	}
	public static BoundingBox fromPositionAndSize(Point pos, double width, double height) {
		return fromPositionAndSize(pos.x, pos.y, width, height);
	}
	
	public static BoundingBox fromCenterAndSize(double x, double y, double width, double height) {
		return fromPositionAndSize(x - width / 2, y - height / 2, width, height);
	}
	public static BoundingBox fromCenterAndSize(Point pos, double width, double height) {
		return fromCenterAndSize(pos.x, pos.y, width, height);
	}
	
	public double getWidth() { return (maxX - minX); }
	public double getHeight() { return (maxY - minY); }
	
	public Point getMin() { return new Point(minX, minY); }
	public Point getMax() { return new Point(maxX, maxY); }
	public Point getCenter() { return new Point((minX + maxX) / 2, (minY + maxY) / 2); }
	
	public BoundingBox move(double x, double y) {
		return new BoundingBox(minX + x, minY + y, maxX + x, maxY + y);
	}
	public BoundingBox move(Point p) { return move(p.x, p.y); }
	
	public BoundingBox mult(double minXFactor, double minYFactor, double maxXFactor, double maxYFactor) {
		return new BoundingBox(minX * minXFactor, minY * minYFactor, maxX * maxXFactor, maxY * maxYFactor);
	}
	public BoundingBox mult(double xFactor, double yFactor) { return mult(xFactor, yFactor, xFactor, yFactor); }
	public BoundingBox mult(double factor) { return mult(factor, factor); }
	
	public BoundingBox resize(double x, double y) {
		return new BoundingBox(minX, minY, maxX + x, maxY + y);
	}
	public BoundingBox resizeCentered(double x, double y) {
		return resize(x, y).move(-x / 2, -y / 2);
	}
	
	/*
	 * Returns the smallest square BoundingBox which encloses the current BoundingBox.
	 * Like this:  +--#==#--+
	 *             '  |  |  '
	 *             '  |  |  '
	 *             '  |  |  '
	 *             +--#==#--+
	 */
	public BoundingBox getOuterSquareBox() {
		double width = getWidth();
		double height = getHeight();
		if (width > height) return resizeCentered(0, width - height);
		else if (height > width) return resizeCentered(height - width, 0);
		else return this;
	}
	
	/*
	 * Returns the largest square BoundingBox which fits inside the current BoundingBox.
	 * Like this:  #===+--------+===#
	 *             |   '        '   |
	 *             |   '        '   |
	 *             |   '        '   |
	 *             #===+--------+===#
	 */
	public BoundingBox getInnerSquareBox() {
		double width = getWidth();
		double height = getHeight();
		if (width > height) return resizeCentered(height - width, 0);
		else if (height > width) return resizeCentered(0, width - height);
		else return this;
	}
	
	public Point relocatePoint(Point p, BoundingBox to) {
		double xFactor = to.getWidth() / getWidth();
		double yFactor = to.getHeight() / getHeight();
		return p.sub(minX, minY).mult(xFactor, yFactor).add(to.minX, to.minY);
	}
	public List<Point> relocatePoints(List<Point> list, BoundingBox to) {
		double xFactor = to.getWidth() / getWidth();
		double yFactor = to.getHeight() / getHeight();
		List<Point> newList = new ArrayList(list.size());
		for (Point p : list)
			newList.add(new Point((p.x - minX) * xFactor + to.minX, (p.y - minY) * yFactor + to.minY));
		return newList;
	}
	
	public static BoundingBox getBoundingBoxOfPoints(List<Point> list) {
		if (list.size() == 0)
			return BoundingBox.zero;
		ListIterator<Point> iterator = list.listIterator();
		Point p = iterator.next();
		double minX = p.x, minY = p.y;
		double maxX = p.x, maxY = p.y;
		for (p = iterator.next(); iterator.hasNext(); p = iterator.next()) {
			if (p.x < minX) minX = p.x;
			if (p.y < minY) minY = p.y;
			if (p.x > maxX) maxX = p.x;
			if (p.y > maxY) maxY = p.y;
		}
		return new BoundingBox(minX, minY, maxX, maxY);
	}
	
	@Override
	public String toString() {
		return String.format(Locale.ENGLISH, "BoundingBox[ %g,%g : %g,%g ]", minX, minY, maxX, maxY);
	}

}
