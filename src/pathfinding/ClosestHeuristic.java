package pathfinding;

/**
 * A heuristic that uses the tile that is closest to the target as the next best tile.
 * 
 * @author Kevin Glass
 */
public class ClosestHeuristic implements AStarHeuristic
{
	/**
	 * @see AStarHeuristic#getCost(TileBasedMap, Mover, int, int, int, int)
	 */
	public float getCost(TileBasedMap map, Mover mover, int x, int y, int tx, int ty)
	{
		float dx = tx - x;
		float dy = ty - y;

		float result = (float) (Math.sqrt((dx * dx) + (dy * dy)));

		return result;
	}

	/*
	 * Taken from this 13 years old website:
	 * 
	 * http://www.flipcode.com/archives/Fast_Approximate_Distance_Functions.shtml
	 */
	public int approximateDistance(int dx, int dy)
	{
		int min, max, approx;

		if (dx < 0)
			dx = -dx;
		if (dy < 0)
			dy = -dy;

		if (dx < dy)
		{
			min = dx;
			max = dy;
		} else
		{
			min = dy;
			max = dx;
		}

		approx = (max * 1007) + (min * 441);
		if (max < (min << 4))
			approx -= (max * 40);

		// add 512 for proper rounding
		return ((approx + 512) >> 10);
	}

}
