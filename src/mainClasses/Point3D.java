package mainClasses;

import java.awt.Point;

public class Point3D //int x and y but double z
{
	public int		x;
	public int		y;
	public double	z;

	public Point3D(int x1, int y1, double z1)
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
