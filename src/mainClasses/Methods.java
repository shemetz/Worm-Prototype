package mainClasses;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * Handy methods for lots of stuff!
 * 
 * @author Itamar
 *
 */
public class Methods
{
	// Handy rotation method
	static GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
	static GraphicsDevice gd = ge.getDefaultScreenDevice();
	static GraphicsConfiguration gc = gd.getDefaultConfiguration();

	/**
	 * Returns rotated BufferedImage. Unused, because {@link Graphics2D#rotate(double, double, double)} is as useful if not more in 99% of the cases.
	 */
	public static BufferedImage rotate(BufferedImage image, double angle)
	{
		if (image == null)
			return null;
		double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
		int w = image.getWidth(), h = image.getHeight();
		int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);

		BufferedImage result = gc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
		Graphics2D g = result.createGraphics();
		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(angle, w / 2, h / 2);
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;
	}

	/**
	 * Compute the dot product AB . BC
	 * <p>
	 * = Distance between point and line
	 * 
	 * @param A
	 * @param B
	 * @param C
	 * @return AB . BC
	 */
	static double DotProduct(Point A, Point B, Point C)
	{
		return (B.x - A.x) * (C.x - B.x) + (B.y - A.y) * (C.y - B.y);
	}

	/**
	 * Compute the cross product AB . BC
	 * 
	 * @param A
	 * @param B
	 * @param C
	 * @return AB X BC
	 */
	static double CrossProduct(Point A, Point B, Point C)
	{
		return (B.x - A.x) * (C.y - A.y) - (B.y - A.y) * (C.x - A.x);
	}

	/**
	 * Compute the squared distance from A to B
	 * 
	 * @param A
	 * @param B
	 * @return |AB|^2 + |BC|^2
	 */
	public static double DistancePow2(Point A, Point B)
	{
		return (A.x - B.x) * (A.x - B.x) + (A.y - B.y) * (A.y - B.y);
	}

	/**
	 * Compute the squared distance from A to B
	 * 
	 * @param A
	 * @param B
	 * @return |AB|^2 + |BC|^2
	 */
	public static double DistancePow2(Point2D A, Point2D B)
	{
		return (A.getX() - B.getX()) * (A.getX() - B.getX()) + (A.getY() - B.getY()) * (A.getY() - B.getY());
	}

	/**
	 * Compute the squared distance from A to B
	 * 
	 * @param ax
	 * @param ay
	 * @param bx
	 * @param by
	 * @return (bx-ax)^2 + (by-ay)^2
	 */
	public static double DistancePow2(double ax, double ay, double bx, double by)
	{
		return (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
	}

	/**
	 * Computes squared distance between a segment and a point
	 * 
	 * @param start
	 * @param end
	 * @param point
	 * @return
	 */
	public static double SegmentToPointDistancePow2(Point3D start, Point3D end, Point point)
	{
		return SegmentToPointDistancePow2(new Point(start.x, start.y), new Point(end.x, end.y), point);
	}

	/**
	 * /** Computes squared distance between a segment and a point
	 * <p>
	 * Casts to integers!
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @param px
	 * @param py
	 * @return
	 */
	public static double SegmentToPointDistancePow2(double x1, double y1, double x2, double y2, double px, double py)
	{
		return SegmentToPointDistancePow2(new Point((int) (x1), (int) (y1)), new Point((int) (x2), (int) (y2)), new Point((int) (px), (int) (py)));
	}

	/**
	 * Computes squared distance between a segment and a point
	 * 
	 * @param start
	 * @param end
	 * @param point
	 * @return
	 */
	public static double SegmentToPointDistancePow2(Point start, Point end, Point point)
	{

		// testing for ends of the line
		double dot1 = DotProduct(start, end, point);
		if (dot1 > 0)
			return DistancePow2(end, point);

		double dot2 = DotProduct(end, start, point);
		if (dot2 > 0)
			return DistancePow2(start, point);
		double distPow2 = Math.pow(CrossProduct(start, end, point), 2) / DistancePow2(start, end);
		return distPow2;
	}

	/**
	 * Finds closest point on segment to a point
	 * <p>
	 * converts to Integer values
	 * 
	 * @param start
	 * @param end
	 * @param point
	 * @return closest point
	 */
	public static Point getClosestRoundedPointOnSegment(Point start, Point end, Point point)
	{
		Point2D p = getClosestPointOnSegment(start.x, start.y, end.x, end.y, point.x, point.y);
		return new Point((int) (p.getX()), (int) (p.getY()));
	}

	/**
	 * Computes closest point to a point on a line segment
	 * 
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 * @param px
	 * @param py
	 * @return
	 */
	public static Point2D getClosestPointOnSegment(double sx1, double sy1, double sx2, double sy2, double px, double py)
	{
		double xDelta = sx2 - sx1;
		double yDelta = sy2 - sy1;

		if ((xDelta == 0) && (yDelta == 0))
		{
			// This is a bug but who cares right
			return new Point2D.Double(sx1, sy1);
		}

		double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0)
		{
			closestPoint = new Point2D.Double(sx1, sy1);
		}
		else if (u > 1)
		{
			closestPoint = new Point2D.Double(sx2, sy2);
		}
		else
		{
			closestPoint = new Point2D.Double(sx1 + u * xDelta, sy1 + u * yDelta);
		}

		return closestPoint;
	}

	/**
	 * Returns closest point on infinite line, defined by two points, and a point.
	 * 
	 * @param sx1
	 * @param sy1
	 * @param sx2
	 * @param sy2
	 * @param px
	 * @param py
	 * @return
	 */
	public static Point2D getClosestPointOnLine(double sx1, double sy1, double sx2, double sy2, double px, double py)
	{
		double xDelta = sx2 - sx1;
		double yDelta = sy2 - sy1;

		if ((xDelta == 0) && (yDelta == 0))
		{
			// oh who cares
			return new Point2D.Double(sx1, sy1);
		}

		double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		return new Point2D.Double(sx1 + u * xDelta, sy1 + u * yDelta);
	}

	/**
	 * 
	 * @param img
	 * @return same image, but with {@link Transparency#TRANSLUCENT}
	 */
	public static BufferedImage optimizeImage(BufferedImage img)
	{
		BufferedImage img2 = gc.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g = img2.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		return img2;
	}

	/**
	 * I dunno
	 * 
	 * @param radius
	 * @param horizontal
	 * @return
	 */
	public static ConvolveOp getGaussianBlurFilter(int radius, boolean horizontal)
	{
		if (radius < 1)
		{
			throw new IllegalArgumentException("Radius must be >= 1");
		}

		int size = radius * 2 + 1;
		float[] data = new float[size];

		float sigma = radius / 3.0f;
		float twoSigmaSquare = 2.0f * sigma * sigma;
		float sigmaRoot = (float) Math.sqrt(twoSigmaSquare * Math.PI);
		float total = 0.0f;

		for (int i = -radius; i <= radius; i++)
		{
			float distance = i * i;
			int index = i + radius;
			data[index] = (float) Math.exp(-distance / twoSigmaSquare) / sigmaRoot;
			total += data[index];
		}

		for (int i = 0; i < data.length; i++)
		{
			data[i] /= total;
		}

		Kernel kernel = null;
		if (horizontal)
		{
			kernel = new Kernel(size, 1, data);
		}
		else
		{
			kernel = new Kernel(1, size, data);
		}
		return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}

	/**
	 * returns point of intersection between two infinite lines, or null if there is none
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static Point2D getLineLineIntersection(Line2D l1, Line2D l2)
	{
		Point2D point = getLineLineIntersection(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(), l2.getX1(), l2.getY1(), l2.getX2(), l2.getY2());
		return point;
	}

	/**
	 * Returns the Point2D of intersection if two segments intersect. Otherwise, returns null.
	 * 
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static Point2D getSegmentIntersection(Line2D l1, Line2D l2)
	{
		double p0_x = l1.getX1(), p1_x = l1.getX2(), p2_x = l2.getX1(), p3_x = l2.getX2();
		double p0_y = l1.getY1(), p1_y = l1.getY2(), p2_y = l2.getY1(), p3_y = l2.getY2();
		double s1_x, s1_y, s2_x, s2_y;
		s1_x = p1_x - p0_x;
		s1_y = p1_y - p0_y;
		s2_x = p3_x - p2_x;
		s2_y = p3_y - p2_y;

		double s, t;
		s = (-s1_y * (p0_x - p2_x) + s1_x * (p0_y - p2_y)) / (-s2_x * s1_y + s1_x * s2_y);
		t = (s2_x * (p0_y - p2_y) - s2_y * (p0_x - p2_x)) / (-s2_x * s1_y + s1_x * s2_y);

		if (s >= 0 && s <= 1 && t >= 0 && t <= 1)
		{
			// Intersection detected
			double i_x = p0_x + (t * s1_x);
			double i_y = p0_y + (t * s1_y);
			return new Point2D.Double(i_x, i_y);
		}

		return null; // No intersection
	}

	/**
	 * Returns point of intersection between two infinite lines, or null if there is none or many
	 * 
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static Point2D getLineLineIntersection(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
	{
		double det1And2 = det(x1, y1, x2, y2);
		double det3And4 = det(x3, y3, x4, y4);
		double x1LessX2 = x1 - x2;
		double y1LessY2 = y1 - y2;
		double x3LessX4 = x3 - x4;
		double y3LessY4 = y3 - y4;
		double det1Less2And3Less4 = det(x1LessX2, y1LessY2, x3LessX4, y3LessY4);
		if (det1Less2And3Less4 == 0)
		{
			// the denominator is zero so the lines are parallel and there's either no solution (or multiple solutions if the lines overlap) so return null.
			return null;
		}
		double x = (det(det1And2, x1LessX2, det3And4, x3LessX4) / det1Less2And3Less4);
		double y = (det(det1And2, y1LessY2, det3And4, y3LessY4) / det1Less2And3Less4);
		return new Point2D.Double(x, y);
	}

	/**
	 * Determinant, I think? Can't remember.
	 * 
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @return a * d - b * c
	 */
	public static double det(double a, double b, double c, double d)
	{
		return a * d - b * c;
	}

	/**
	 * Distance squared...you know
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double DistancePow2(Point3D a, Point2D b)
	{
		return DistancePow2((int) a.x, (int) a.y, (int) b.getX(), (int) b.getY());
	}

	/**
	 * Returns distance squared. Why are you even reading this?
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static double DistancePow2(Point3D a, Point3D b)
	{
		return DistancePow2((int) a.x, (int) a.y, (int) b.x, (int) b.y);
	}

	/**
	 * Returns an angle that's between angle and target, close to angle but leaning towards target depending on amount.
	 * 
	 * @param angle
	 *            in radians
	 * @param target
	 *            in radians
	 * @param amount
	 * @return lerped angle in radians
	 */
	public static double lerpAngle(double angle, double target, double amount)
	{
		// TODO um. Does it accept all angles, or only ones between 0 and tau, or only ones between -pi and +pi?
		return angle + (((((target - angle) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * amount;
	}

	/**
	 * 
	 * @param angle1
	 * @param angle2
	 * @return angle between the two angles. If they are opposed, returns NaN
	 */
	public static double meanAngle(double angle1, double angle2)
	{
		// solution also works on more than 2 angles)
		double xSum = Math.cos(angle1) + Math.cos(angle2);
		double ySum = Math.sin(angle1) + Math.sin(angle2);
		if (xSum == 0 && ySum == 0)
			return Double.NaN; // angles cancel each other out
		else
			return Math.atan2(ySum / 2, xSum / 2);
	}

	/**
	 * Tints an image with a color (tints a new copy of the image and returns it)
	 * 
	 * @param original
	 * @param color
	 * @return
	 */
	public BufferedImage tint(BufferedImage original, Color color)
	{
		BufferedImage result = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D buffy = result.createGraphics();
		buffy.drawImage(original, 0, 0, null);
		buffy.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN, 1)); // 1 = opacity.
		buffy.setColor(color);
		buffy.fillRect(0, 0, original.getWidth(), original.getHeight());
		buffy.dispose();
		return result;
	}

	/**
	 * Returns the closest intersection point of a line and a rectangle. If there is none, returns null.
	 * 
	 * @param line
	 * @param rectangle
	 * @return
	 */
	public static Point2D getClosestIntersectionPoint(Line2D line, Rectangle2D rectangle)
	{
		// if (Methods.LineToPointDistancePow2(line.getX1(), line.getY1(), line.getX2(), line.getY2(), rectangle.getCenterX(), rectangle.getCenterY()) > rectangle.getWidth() * rectangle.getHeight() / 4)
		// return null;
		Point2D[] p = new Point2D[4];

		// Top line
		p[0] = getSegmentIntersection(line, new Line2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getX() + rectangle.getWidth(), rectangle.getY()));
		// Bottom line
		p[1] = getSegmentIntersection(line,
				new Line2D.Double(rectangle.getX(), rectangle.getY() + rectangle.getHeight(), rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight()));
		// Left side...
		p[2] = getSegmentIntersection(line, new Line2D.Double(rectangle.getX(), rectangle.getY(), rectangle.getX(), rectangle.getY() + rectangle.getHeight()));
		// Right side
		p[3] = getSegmentIntersection(line,
				new Line2D.Double(rectangle.getX() + rectangle.getWidth(), rectangle.getY(), rectangle.getX() + rectangle.getWidth(), rectangle.getY() + rectangle.getHeight()));

		// find closest to line's start
		double maxDistPow2 = Double.MAX_VALUE;
		Point2D result = null;
		for (Point2D point : p)
			if (point != null)
			{
				double distPow2 = Methods.DistancePow2(line.getX1(), line.getY1(), point.getX(), point.getY());
				if (distPow2 < maxDistPow2)
				{
					maxDistPow2 = distPow2;
					result = point;
				}
			}

		return result;
	}
}
