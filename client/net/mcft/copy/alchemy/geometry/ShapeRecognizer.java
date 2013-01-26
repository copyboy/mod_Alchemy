package client.net.mcft.copy.alchemy.geometry;

import java.util.*;

public abstract class ShapeRecognizer {

	public static List<ShapeRecognizer> recognizers = new LinkedList<ShapeRecognizer>();
	static {
		recognizers.add(ShapeCircle.getShapeRecognizer());
	}
	
	public abstract Shape recognize(List<Point> list);
	
	/*
	 * Tries to recognize a shape. Works best with points first reduced to characteristic ones.
	 * Returns null if recognition failed.
	 */
	public static Shape recognizeShape(List<Point> list) {
		for (ShapeRecognizer recognizer : recognizers) {
			Shape shape = recognizer.recognize(list);
			if (shape != null)
				return shape;
		}
		return null;
	}
	
	static final double defaultMinAngle = 15;
	static final double defaultMinDistance = 0.075;
	/*
	 * Returns a new list with characteristic points for easier shape recognition.
	 * Proudly "copied" from http://www.ii.pwr.wroc.pl/~piasecki/publications/hofman-piasecki-v1-1.pdf
	 */
	public static List<Point> reduceToCharPoints(List<Point> list, double minAngle, double minDistance) {
		minAngle = Math.toRadians(minAngle);
		List<Point> reducedPointList = new LinkedList<Point>();
		Point lastCharPoint = list.get(0);
		Point lastPoint = list.get(list.size() - 1);
		reducedPointList.add(lastCharPoint);
		for (int i = 1; i < list.size() - 1; i++) {
			Point p = list.get(i);
			Point next = list.get(i + 1);
			if (Math.abs(Point.angleDifference(lastCharPoint.angleTo(p), p.angleTo(next))) > minAngle &&
			    p.distanceTo(lastCharPoint) > minDistance)
				reducedPointList.add(lastCharPoint = p);
		}
		reducedPointList.add(lastPoint);
		return reducedPointList;
	}
	/*
	 * Returns a new list with characteristic points for easier shape recognition,
	 * using the default values of minAngle: 25, minDistance: 0.1.
	 * Proudly "copied" from http://www.ii.pwr.wroc.pl/~piasecki/publications/hofman-piasecki-v1-1.pdf
	 */
	public static List<Point> reduceToCharPoints(List<Point> list) {
		return reduceToCharPoints(list, defaultMinAngle, defaultMinDistance);
	}

}
