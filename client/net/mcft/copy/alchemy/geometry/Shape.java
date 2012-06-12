package net.mcft.copy.alchemy.geometry;

public abstract class Shape {

	public abstract BoundingBox getBoundingBox();
	
	public Point getCenter() { return getBoundingBox().getCenter(); }

}
