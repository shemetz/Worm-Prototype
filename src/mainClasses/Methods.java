package mainClasses;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Transparency;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class Methods
{
	// Handy rotation method
	public static BufferedImage rotate(BufferedImage image, double angle, Frame that)
	{
		double sin = Math.abs(Math.sin(angle)), cos = Math.abs(Math.cos(angle));
		int w = image.getWidth(), h = image.getHeight();
		int neww = (int) Math.floor(w * cos + h * sin), newh = (int) Math.floor(h * cos + w * sin);
		GraphicsConfiguration tgc = that.getGraphicsConfiguration();
		BufferedImage result = tgc.createCompatibleImage(neww, newh, Transparency.TRANSLUCENT);
		Graphics2D g = result.createGraphics();
		g.translate((neww - w) / 2, (newh - h) / 2);
		g.rotate(angle, w / 2, h / 2);
		g.drawRenderedImage(image, null);
		g.dispose();
		return result;
	}

	// Distance between point and line
	// Compute the dot product AB . BC
	static double DotProduct(Point a, Point b, Point c)
	{
		return (b.x - a.x) * (c.x - b.x) + (b.y - a.y) * (c.y - b.y);
	}

	// Compute the cross product AB x AC
	static double CrossProduct(Point a, Point b, Point c)
	{
		return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x);
	}

	// Compute the squared distance from A to B
	public static double DistancePow2(Point a, Point b)
	{
		return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
	}

	// Compute the squared distance from A to B
	public static double DistancePow2(Point2D a, Point2D b)
	{
		return (a.getX() - b.getX()) * (a.getX() - b.getX()) + (a.getY() - b.getY()) * (a.getY() - b.getY());
	}

	public static double DistancePow2(double ax, double ay, double bx, double by)
	{
		return (ax - bx) * (ax - bx) + (ay - by) * (ay - by);
	}

	// Compute the SQUARED distance from AB to C
	public static double LineToPointDistancePow2(Point start, Point end, Point point)
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

	public static Point getClosestRoundedPointOnSegment(Point start, Point end, Point point)
	{
		Point2D p = getClosestPointOnSegment(start.x, start.y, end.x, end.y, point.x, point.y);
		return new Point((int) (p.getX()), (int) (p.getY()));
	}

	// returns closest point on AB to the point X
	public static Point2D getClosestPointOnSegment(double sx1, double sy1, double sx2, double sy2, double px, double py)
	{
		double xDelta = sx2 - sx1;
		double yDelta = sy2 - sy1;

		if ((xDelta == 0) && (yDelta == 0))
		{
			Main.errorMessage("That's not a line");
			return null;
		}

		double u = ((px - sx1) * xDelta + (py - sy1) * yDelta) / (xDelta * xDelta + yDelta * yDelta);

		final Point2D closestPoint;
		if (u < 0)
		{
			closestPoint = new Point2D.Double(sx1, sy1);
		} else if (u > 1)
		{
			closestPoint = new Point2D.Double(sx2, sy2);
		} else
		{
			closestPoint = new Point2D.Double(sx1 + u * xDelta, sy1 + u * yDelta);
		}

		return closestPoint;
	}

	// Compute the dot product AB . AC
	public static double realDotProduct(Point a, Point b, Point c)
	{
		return (b.x - a.x) * (c.x - a.x) + (b.y - a.y) * (c.y - a.y);
	}

	static GraphicsDevice			gd	= GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	static GraphicsConfiguration	gc	= gd.getDefaultConfiguration();

	public static BufferedImage optimizeImage(BufferedImage img)
	{
		BufferedImage img2 = gc.createCompatibleImage(img.getWidth(), img.getHeight(), Transparency.TRANSLUCENT);
		Graphics2D g = img2.createGraphics();
		g.drawImage(img, 0, 0, null);
		g.dispose();

		return img2;
	}

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
		} else
		{
			kernel = new Kernel(1, size, data);
		}
		return new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
	}

	public static Point2D getLineLineIntersection(Line2D l1, Line2D l2)
	{
		Point2D point = getLineLineIntersection(l1.getX1(), l1.getY1(), l1.getX2(), l1.getY2(), l2.getX1(), l2.getY1(), l2.getX2(), l2.getY2());
		return point;
	}

	/**
	 * test.
	 * 
	 * blah
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
			// Collision detected
			double i_x = p0_x + (t * s1_x);
			double i_y = p0_y + (t * s1_y);
			return new Point2D.Double(i_x, i_y);
		}

		return null; // No collision
	}

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

	public static double det(double a, double b, double c, double d)
	{
		return a * d - b * c;
	}

	public static double DistancePow2(Point3D a, Point2D b)
	{
		return DistancePow2((int) a.x, (int) a.y, (int) b.getX(), (int) b.getY());
	}

	public static double lerpAngle(double angle, double target, double amount)
	{
		return angle + (((((target - angle) % (Math.PI * 2)) + (Math.PI * 3)) % (Math.PI * 2)) - Math.PI) * amount;
	}

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
}
