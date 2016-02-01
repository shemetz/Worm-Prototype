package pathfinding;

import mainClasses.Environment;

public class EnvMap implements TileBasedMap
{
	boolean[][] grid;
	int width, height;
	
	public EnvMap(Environment env)
	{
		width = env.width;
		height = env.height;
		grid = new boolean[width][height];
		for (int x = 0; x < env.width; x++)
			for (int y = 0; y < env.height; y++)
				if (env.wallTypes[x][y] != -1)
					grid[x][y] = true;
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
		// TODO Auto-generated method stub
	}

	public boolean blocked(Mover mover, int x, int y)
	{
		return grid[x][y];
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty)
	{
		return 1;
	}

}
