package airplane.g2.util;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

public class PointUtil {

	public static Point2D.Double addPoints(Point2D.Double point1, Point2D.Double point2) {
		return new Point2D.Double(point1.x + point2.x, point1.y + point2.y);
	}
	
	public static Point2D.Double midpoint(Point2D.Double a, Point2D.Double b) {
		return new Point2D.Double((a.x+b.x)/2.0, (a.y+b.y)/2.0);
	}
	
}
