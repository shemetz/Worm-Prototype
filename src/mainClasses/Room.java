package mainClasses;

import java.awt.Rectangle;

public class Room extends Rectangle
{
	// used for procedural generation
	int origin;

	public Room(int x1, int y1, int width1, int height1, int origin1)
	{
		super(x1, y1, width1, height1);
		origin = origin1;
	}
}
