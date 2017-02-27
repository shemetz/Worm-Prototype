package pathfinding;

import java.awt.Point;

import effects.E_Resistant;
import mainClasses.EP;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Furniture;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Portal;

public class EnvMap implements TileBasedMap
{
	public final int SQUARE = 96;
	public boolean[][] walls;
	public int[][] poolTypes;
	public boolean[][] FFs;
	public Point[][] portals; // the coordinates that being in this tile will get you to
	public int width, height;

	public EnvMap(Environment env)
	{
		// Only in Z == 0 !!!

		width = env.width;
		height = env.height;
		walls = new boolean[width][height];
		poolTypes = new int[width][height];
		FFs = new boolean[width][height];
		portals = new Point[width][height];

		for (int x = 0; x < env.width; x++)
			for (int y = 0; y < env.height; y++)
			{
				walls[x][y] = env.wallTypes[x][y] != -1;
				poolTypes[x][y] = env.poolTypes[x][y];
				FFs[x][y] = false;
				portals[x][y] = null;
			}
		for (Furniture f : env.furniture)
			for (Point p : f.getPoints())
			{
				// uses corner points slightly pulled towards the center, in order to not accidentally mark adjacent squares
				int x = (int) (p.x + 1 * Math.cos(f.x - p.x)) / SQUARE;
				int y = (int) (p.y + 1 * Math.sin(f.y - p.y)) / SQUARE;
				if (x >= 0 && y >= 0 && x < width && y < height)
				{
					if (f.type == Furniture.Type.DOOR && f.state != 2) // not locked door
						continue;
					walls[x][y] = true;
				}
			}
		double squareroot2by2 = SQUARE * Math.sqrt(0.5);
		for (ForceField ff : env.FFs)
		{
			if (ff.z < 1)
				for (int x = (int) (ff.x - ff.length / 2) / SQUARE; x <= (int) (ff.x + ff.length / 2) / SQUARE; x++)
					for (int y = (int) (ff.y - ff.length / 2) / SQUARE; y <= (int) (ff.y + ff.length / 2) / SQUARE; y++)
						if (x >= 0 && y >= 0 && x < width && y < height)
						{
							if (Methods.SegmentToPointDistancePow2(ff.p[0].x, ff.p[0].y, ff.p[1].x, ff.p[1].y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < Math
									.pow(squareroot2by2 + ff.width / 2, 2))
								FFs[x][y] = true; // TODO make it real
							else if (Methods.SegmentToPointDistancePow2(ff.p[1].x, ff.p[1].y, ff.p[2].x, ff.p[2].y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < Math
									.pow(squareroot2by2 + ff.width / 2, 2))
								FFs[x][y] = true; // TODO make it real
							else if (Methods.SegmentToPointDistancePow2(ff.p[2].x, ff.p[2].y, ff.p[3].x, ff.p[3].y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < Math
									.pow(squareroot2by2 + ff.width / 2, 2))
								FFs[x][y] = true; // TODO make it real
							else if (Methods.SegmentToPointDistancePow2(ff.p[3].x, ff.p[3].y, ff.p[0].x, ff.p[0].y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < Math
									.pow(squareroot2by2 + ff.width / 2, 2))
								FFs[x][y] = true; // TODO make it real

						}
		}
		for (Portal p : env.portals)
			if (p.z < 1)
				if (p.partner != null)
					if (p.partner.z < 1)
					{
						for (int x = (int) (p.x - p.length / 2) / SQUARE; x <= (int) (p.x + p.length / 2) / SQUARE; x++)
							for (int y = (int) (p.y - p.length / 2) / SQUARE; y <= (int) (p.y + p.length / 2) / SQUARE; y++)
								if (x >= 0 && y >= 0 && x < width && y < height)
									if (Methods.SegmentToPointDistancePow2(p.start.x, p.start.y, p.end.x, p.end.y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < SQUARE / 2 * SQUARE / 2)
									{
										if (Methods.DistancePow2(p.start.x, p.start.y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < SQUARE / 2 * SQUARE / 2)
										{
											walls[x][y] = true; // portal tips are basically walls
										}
										else if (Methods.DistancePow2(p.end.x, p.end.y, x * SQUARE + SQUARE / 2, y * SQUARE + SQUARE / 2) < SQUARE / 2 * SQUARE / 2)
										{
											walls[x][y] = true; // portal tips are basically walls
										}
										else
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
		if (x < 0 || y < 0 || x >= width || y >= height)
			return true;
		if (walls[x][y])
			return true;
		if (FFs[x][y])
			return true;
		return false;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty)
	{
		int cost = 1;
		if (walls[tx][ty])
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
			//If the person has resistance to the element, ignore
			if (mover instanceof Person)
				for (Effect e : ((Person)mover).effects)
					if (e instanceof E_Resistant)
						if (EP.damageType(((E_Resistant)e).element) == EP.damageType(poolTypes[tx][ty]))
							break;
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
