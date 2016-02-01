package pathfinding;

import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Person;

public class EnvMap implements TileBasedMap
{
	final int	SQUARE	= 96;
	int[][]		wallTypes;
	int[][]		poolTypes;
	boolean[][]	FFs;
	int			width, height;

	public EnvMap(Environment env)
	{
		width = env.width;
		height = env.height;
		wallTypes = new int[width][height];
		poolTypes = new int[width][height];
		FFs = new boolean[width][height];

		for (int x = 0; x < env.width; x++)
			for (int y = 0; y < env.height; y++)
			{
				wallTypes[x][y] = env.wallTypes[x][y];
				poolTypes[x][y] = env.poolTypes[x][y];
			}
		for (ForceField ff : env.FFs)
		{
			for (int x = (int) (ff.x - ff.length / 2) / SQUARE; x < (int) (ff.x + ff.length / 2) / SQUARE; x++)
				for (int y = (int) (ff.y - ff.length / 2) / SQUARE; y < (int) (ff.y + ff.length / 2) / SQUARE; y++)
					FFs[x][y] = true; // TODO make it real
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

		return cost;
	}

}
