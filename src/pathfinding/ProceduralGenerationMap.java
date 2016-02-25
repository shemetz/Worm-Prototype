package pathfinding;

import java.util.List;

import mainClasses.Room;

public class ProceduralGenerationMap implements TileBasedMap
{
	int[][] grid;
	int width, height;
	List<Room> rooms;

	public ProceduralGenerationMap(int width1, int height1, List<Room> rooms1, int[][] walls)
	{
		width = width1;
		height = height1;
		rooms = rooms1;
		grid = new int[width][height];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
			{
				grid[i][j] = 0;
				if (walls[i][j] > 0)
					grid[i][j] = 100;
			}
		for (Room r : rooms)
		{
			for (int x = r.x; x <= r.x + r.width; x++)
				grid[x][r.y] = 100;
			for (int y = r.y; y <= r.y + r.height; y++)
				grid[r.x][y] = 100;
			for (int x = r.x; x <= r.x + r.width; x++)
				grid[x][r.y + r.height] = 100;
			for (int y = r.y; y <= r.y + r.height; y++)
				grid[r.x + r.width][y] = 100;
			// around room - better not
			for (int x = r.x - 1; x <= r.x + r.width; x++)
				if (r.y > 0)
					grid[x][r.y - 1] = 10;
			for (int y = r.y - 1; y <= r.y + r.height; y++)
				if (r.x > 0)
					grid[r.x - 1][y] = 10;
			for (int x = r.x - 1; x <= r.x + r.width; x++)
				if (r.y + r.height < height)
					grid[x][r.y + r.height] = 10;
			for (int y = r.y - 1; y <= r.y + r.height; y++)
				if (r.x + r.width < width)
					grid[r.x + r.width][y] = 10;
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
		if (x < 0 || y < 0 || x >= width || y >= height)
			return true;
		if (grid[x][y] == 100)
			return true;
		return false;
	}

	public float getCost(Mover mover, int sx, int sy, int tx, int ty)
	{
		return grid[tx][ty];
	}

}
