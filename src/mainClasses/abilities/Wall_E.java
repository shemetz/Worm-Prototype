package mainClasses.abilities;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Wall_E extends Ability
{
	// Hahahahahahahaha
	//
	// ...
	//
	// "Wall E"
	//

	final int squareSize = 96;

	public Wall_E(String elementName, int p)
	{
		super("Wall <" + elementName + ">", p);
	}

	public void use(Environment env, Person user, Point target)
	{

		/*
		 * Create a wall. Destroys any pools in that area.
		 */
		int gridX = target.x / squareSize, gridY = target.y / squareSize;
		boolean repairingWall = env.wallTypes[gridX][gridY] == getElementNum() && env.wallHealths[gridX][gridY] < 100 && 0.3 <= user.mana;
		if (!on && !user.maintaining && (cost <= user.mana || repairingWall))
		{
			boolean canCreate = true;
			if (!repairingWall)
			{
				// stop creating wall if there already is a different wall there
				if (env.wallTypes[gridX][gridY] != -1)
					if (env.wallTypes[gridX][gridY] != getElementNum() || (env.wallTypes[gridX][gridY] == getElementNum() && env.wallHealths[gridX][gridY] >= 100))
						canCreate = false;
				// stop creating wall if it collides with someone
				for (Person p : env.people)
				{
					if ((int) (p.x - 0.5 * p.radius) / squareSize == gridX && (int) (p.y - 0.5 * p.radius) / squareSize == gridY)
						canCreate = false;
					if ((int) (p.x + 0.5 * p.radius) / squareSize == gridX && (int) (p.y - 0.5 * p.radius) / squareSize == gridY)
						canCreate = false;
					if ((int) (p.x - 0.5 * p.radius) / squareSize == gridX && (int) (p.y + 0.5 * p.radius) / squareSize == gridY)
						canCreate = false;
					if ((int) (p.x + 0.5 * p.radius) / squareSize == gridX && (int) (p.y + 0.5 * p.radius) / squareSize == gridY)
						canCreate = false;
				}
			}
			if (canCreate)
			{
				// starting the wall
				targetEffect1 = gridX;
				targetEffect2 = gridY;
				targetEffect3 = 0; // able
				env.addWall(gridX, gridY, getElementNum(), false);
				env.wallHealths[gridX][gridY] = Math.max(env.wallHealths[gridX][gridY], 1);
				if (repairingWall)
					user.mana -= 0.3;
				else
					user.mana -= cost;
				user.maintaining = true;
				user.notAnimating = true;
				cooldownLeft = cooldown;
				on = true;
				user.switchAnimation(2);
			} else
				targetEffect3 = 1; // unable
		} else if (on && user.maintaining)
		{
			if (env.wallHealths[target.x / squareSize][target.y / squareSize] > 90)
				env.wallHealths[target.x / squareSize][target.y / squareSize] = 100; // to fix some problems
			env.connectWall(target.x / squareSize, target.y / squareSize);
			// finishing the wall
			on = false;
			user.maintaining = false;
			user.notAnimating = false;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{

		player.rangeArea = new Area();
		for (int i = (int) (player.x - range); i < (int) (player.x + range); i += squareSize)
			for (int j = (int) (player.y - range); j < (int) (player.y + range); j += squareSize)
				if (Math.pow(player.x - i / squareSize * squareSize - 0.5 * squareSize, 2) + Math.pow(player.y - j / squareSize * squareSize - 0.5 * squareSize, 2) <= range * range)
					player.rangeArea.add(new Area(new Rectangle2D.Double(i / squareSize * squareSize, j / squareSize * squareSize, squareSize, squareSize)));

		int gridX = player.target.x / squareSize, gridY = player.target.y / squareSize;

		player.targetType = "createInGrid";
		if (!player.maintaining)
		{
			boolean canCreate = true;
			if (env.wallTypes[gridX][gridY] != -1)
				if (env.wallTypes[gridX][gridY] != getElementNum() || (env.wallTypes[gridX][gridY] == getElementNum() && env.wallHealths[gridX][gridY] >= 100))
					canCreate = false;
			// stop creating wall if it collides with someone
			for (Person p : env.people)
			{
				if ((int) (p.x - 0.5 * p.radius) / squareSize == gridX && (int) (p.y - 0.5 * p.radius) / squareSize == gridY)
					canCreate = false;
				if ((int) (p.x + 0.5 * p.radius) / squareSize == gridX && (int) (p.y - 0.5 * p.radius) / squareSize == gridY)
					canCreate = false;
				if ((int) (p.x - 0.5 * p.radius) / squareSize == gridX && (int) (p.y + 0.5 * p.radius) / squareSize == gridY)
					canCreate = false;
				if ((int) (p.x + 0.5 * p.radius) / squareSize == gridX && (int) (p.y + 0.5 * p.radius) / squareSize == gridY)
					canCreate = false;
			}
			targetEffect3 = canCreate ? 0 : 1;
			targetEffect1 = gridX;
			targetEffect2 = gridY;
			player.target = new Point((int) gridX * squareSize + squareSize / 2, (int) gridY * squareSize + squareSize / 2);
		} else // don't change target
			player.target = new Point((int) targetEffect1 * squareSize + squareSize / 2, (int) targetEffect2 * squareSize + squareSize / 2);
	}
}
