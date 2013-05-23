package airplane.g2.util;

import java.awt.geom.Point2D;

public class PointUtil {

	public static Point2D.Double addPoints(Point2D.Double point1, Point2D.Double point2) {
		return new Point2D.Double(point1.x + point2.x, point1.y + point2.y);
	}
}
