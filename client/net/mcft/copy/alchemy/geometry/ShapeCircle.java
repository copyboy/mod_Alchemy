package net.mcft.copy.alchemy.geometry;

import java.util.List;
import java.util.ListIterator;

public class ShapeCircle extends Shape {

	public final double x, y, radius;
	
	public ShapeCircle(double x, double y, double radius) {
		this.x = x;
		this.y = y;
		this.radius = radius;
	}
	public ShapeCircle(Point p, double radius) { this(p.x, p.y, radius); }
	
	@Override
	public BoundingBox getBoundingBox() {
		return new BoundingBox(x - radius, y - radius, x + radius, y + radius);
	}
	
	// TODO: Add check for angles between points.
	static double maxAngle = 60;
	static double maxDistance = 0.3;
	public static ShapeRecognizer getShapeRecognizer() {
		return new ShapeRecognizer(){
			@Override
			public Shape recognize(List<Point> list) {
				if (list.size() < 6)
					// Return if there's less than 6 points.
					return null;
				BoundingBox original = BoundingBox.getBoundingBoxOfPoints(list).getOuterSquareBox();
				// Scale points to fit in BoundingBox[ -1,-1 : 1,1 ].
				List<Point> points = original.relocatePoints(list, BoundingBox.identity);
				double totalAngle = 0;
				Point p = points.get(0);
				double lastAngle = Point.zero.angleTo(p);
				double minDis = Point.zero.distanceTo(p);
				double avgDis = minDis;
				double maxDis = minDis;
				int direction = 0;
				for (int i = 1; i < list.size(); i++) {
					p = points.get(i);
					double angle = Point.zero.angleTo(p);
					double angDif = Point.angleDifference(lastAngle, angle);
					if (direction == 0) direction = (angDif > 0 ? 1 : -1);
					else if (Math.signum(angDif) != direction)
						// Return if the user can't decide if e wants to draw clockwise or counter-clockwise :P
						return null;
					double distance = Point.zero.distanceTo(p);
					if (distance < minDis) minDis = distance;
					else if (distance > maxDis) maxDis = distance;
					if (maxDis - minDis > maxDistance)
						// Return if the distance of the points to the center is too different.
						return null;
					avgDis += distance;
					totalAngle += Math.abs(angDif);
					if (totalAngle > Math.PI * 2.2)
						// Return if line length is more than a circle.
						return null;
					lastAngle = angle;
				}
				if (totalAngle < Math.PI * 1.8)
					// Return if line length is not enough to form a full circle.
					return null;
				avgDis /= list.size();
				return new ShapeCircle(original.getCenter(), avgDis * original.getWidth() / 2);
			}
		};
	}

}
