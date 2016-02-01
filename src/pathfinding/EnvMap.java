package pathfinding;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Portal;

public class EnvMap implements TileBasedMap
{
	public final int	SQUARE	= 96;
	public int[][]		wallTypes;
	public int[][]		poolTypes;
	public boolean[][]	FFs;
	public Point[][]	portals;		// the coordinates that being in this tile will get you to
	public int			width, height;

	public EnvMap(Environment env)
	{
		width = env.width;
		height = env.height;
		wallTypes = new int[width][height];
		poolTypes = new int[width][height];
		FFs = new boolean[width][height];
		portals = new Point[width][height];

		for (int x = 0; x < env.width; x++)
			for (int y = 0; y < env.height; y++)
			{
				wallTypes[x][y] = env.wallTypes[x][y];
				poolTypes[x][y] = env.poolTypes[x][y];
				FFs[x][y] = false;
				portals[x][y] = null;
			}
		for (ForceField ff : env.FFs)
		{
			for (int x = (int) (ff.x - ff.length / 2) / SQUARE; x <= (int) (ff.x + ff.length / 2) / SQUARE; x++)
				for (int y = (int) (ff.y - ff.length / 2) / SQUARE; y <= (int) (ff.y + ff.length / 2) / SQUARE; y++)
					FFs[x][y] = true; // TODO make it real
		}
		for (Portal p : env.portals)
			if (p.partner != null)
			{
				for (int x = (int) (p.x - p.length / 2) / SQUARE; x <= (int) (p.x + p.length / 2) / SQUARE; x++)
					for (int y = (int) (p.y - p.length / 2) / SQUARE; y <= (int) (p.y + p.length / 2) / SQUARE; y++)
						if (Methods.getSegmentPointDistancePow2(p.start.x, p.start.y, p.end.x, p.end.y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < SQUARE / 2 * SQUARE / 2)
						{
							if (Methods.DistancePow2(p.start.x, p.start.y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < SQUARE / 2 * SQUARE / 2)
							{
								wallTypes[x][y] = -2; // portal tips are basically walls
							} else if (Methods.DistancePow2(p.end.x, p.end.y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < SQUARE / 2 * SQUARE / 2)
							{
								wallTypes[x][y] = -2; // portal tips are basically walls
							} else
							{
								// the coordinates that being in this tile will get you to
								double angleChange = p.partner.angle - p.angle;
								double angleRelativeToPortal = Math.atan2((y + 0.5) * SQUARE - p.y, (x + 0.5) * SQUARE - p.x);
								double distanceRelativeToPortal = Math.sqrt(Methods.DistancePow2(p.x, p.y, (x + 0.5) * SQUARE, (y + 0.5) * SQUARE));
								double destinationX = p.partner.x + distanceRelativeToPortal * Math.cos(angleRelativeToPortal + angleChange);
								double destinationY = p.partner.y + distanceRelativeToPortal * Math.sin(angleRelativeToPortal + angleChange);
								portals[x][y] = new Point((int) (destinationX / SQUARE), (int) (destinationY / SQUARE));
							}
						}
			}
	}

	public int getWidthInTiles()
	{
		return width;
	}

	public int getHeightInTiles()
	{
		return height;
	}

	public void pathFinderVisited(int x, int y)
	{

	}

	public boolean blocked(Mover mover, int x, int y)
	{
		if (mover instanceof Person)
			if (((Person) mover).ghostMode)
				return false;
		if (wallTypes[x][y] != -1)
			return true;
		if (FFs[x][y])
			return true;
		return false;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty)
	{
		int cost = 1;
		if (wallTypes[tx][ty] != -1)
			cost += 100;
		if (FFs[tx][ty])
			cost += 80;
		switch (poolTypes[tx][ty])
		{
		case 0:
		case 2:
		case 3:
		case 4:
		case 6:
			return -1; // those things don't even exist
		case 1: // water
			cost += 3;
			break;
		case 5: // ice
			cost += 2;
			break;
		case 7: // acid
		case 8: // lava
			cost += 30;
			break;
		case 9: // flesh/blood
			cost += 3;
			break;
		case 10: // earth spikes
			cost += 10;
			break;
		case 11: // plant/vines
			cost += 10;
			break;
		default:
			break;
		}

		if (portals[tx][ty] != null)
			cost += 20; // to make NPCs stop pretending they're smart enough to handle thinking with portals

		return cost;
	}

}
