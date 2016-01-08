package shouldBeDefault;
import java.awt.Point;

public class Point3D
{
	public int x;
	public int y;
	public int z;
	public Point3D (int x1, int y1, int z1)
	{
		x = x1;
		y = y1;
		z = z1;
	}
	public Point Point()
	{
		return new Point(x, y);
	}
}
