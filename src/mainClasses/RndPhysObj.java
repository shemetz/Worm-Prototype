package mainClasses;

import java.awt.Point;

public class RndPhysObj extends Drawable
{
	public double xVel, yVel; // per second
	public double angularVelocity; // radians per second
	public double mass; // in kilograms
	public double zVel; // positive = upwards
	public double timeEffect = 1; // if 0.4, this will move 40% as fast and apply 40% the force when colliding and will only be dealt 40% of the damage

	public RndPhysObj(double x1, double y1, int r1, double m1)
	{
		super();
		x = x1;
		y = y1;
		radius = r1;
		xVel = 0;
		yVel = 0;
		mass = m1;
		angularVelocity = 0;
		zVel = 0;
	}

	public Point Point()
	{
		return new Point((int) x, (int) y);
	}

	public double velocity()
	{
		return Math.sqrt(xVel * xVel + yVel * yVel);
	}

	public double angle()
	{
		return Math.atan2(yVel, xVel);
	}
}
