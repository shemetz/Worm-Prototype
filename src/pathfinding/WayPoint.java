package pathfinding;

import java.awt.Point;

public class WayPoint extends Point
{
	public boolean byPortal;
	public boolean portalWasUsed = false;

	public WayPoint(int x1, int y1)
	{
		x = x1;
		y = y1;
		byPortal = false;
	}

	public WayPoint(int x1, int y1, boolean bp)
	{
		x = x1;
		y = y1;
		byPortal = bp;
	}
}
