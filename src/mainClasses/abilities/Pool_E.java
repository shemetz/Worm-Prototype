package mainClasses.abilities;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Pool_E extends Ability
{

	final int squareSize = 96;

	public Pool_E(String elementName, int p)
	{
		super("Pool <" + elementName + ">", p);
		cost = Math.max(3 - 0.3 * points, 0.8); // reduced cost is that minus 1.5
		costPerSecond = 1;
		costType = "mana";
		cooldown = Math.max(3 - 0.3 * points, 0.3); // is used for creating the pool
		targetEffect1 = -1; // x grid position
		targetEffect2 = -1; // y grid position
		range = 600;
		rangeType = "Create in grid";
		maintainable = true;
		instant = true;
	}

	public void use(Environment env, Person user, Point target)
	{
		/*
		 * Create a pool. Destroys any walls in that area. The cost of creating a pool is reduced if there's an adjacent pool.
		 */
		int gridX = target.x / squareSize, gridY = target.y / squareSize;
		// test for lesser cost
		boolean lesserCost = false;
		for (int i = gridX - 1; i <= gridX + 1; i++)
			for (int j = gridY - 1; j <= gridY + 1; j++)
				if (env.poolTypes[i][j] == getElementNum())
					lesserCost = true;
		if (!on && !user.maintaining && (cost <= user.mana || (lesserCost && Math.max(cost - 2, 0) < user.mana)))
		{
			boolean canCreate = true;
			// stop creating pool if there already is a different pool there
			if (env.poolTypes[gridX][gridY] != -1)
				if (env.poolTypes[gridX][gridY] != getElementNum() || (env.poolTypes[gridX][gridY] == getElementNum() && env.poolHealths[gridX][gridY] >= 100))
					canCreate = false;
			// stop creating pool if it collides with someone
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
			if (canCreate)
			{
				if (lesserCost)
					user.mana -= Math.max(cost - 1.5, 0);
				else
					user.mana -= cost;
				// starting the pool
				targetEffect1 = gridX;
				targetEffect2 = gridY;
				targetEffect3 = 0; // able
				env.addPool(gridX, gridY, getElementNum(), false);
				env.poolHealths[gridX][gridY] = Math.max(env.poolHealths[gridX][gridY], 1);
				user.maintaining = true;
				user.notAnimating = true;
				cooldownLeft = cooldown;
				on = true;
				user.switchAnimation(2);
			} else
				targetEffect3 = 1; // unable
		} else if (on && user.maintaining)
		{
			if (env.poolHealths[target.x / squareSize][target.y / squareSize] > 90)
				env.poolHealths[target.x / squareSize][target.y / squareSize] = 100; // to fix some problems
			env.connectPool(target.x / squareSize, target.y / squareSize);
			// finishing the pool
			on = false;
			user.maintaining = false;
			user.notAnimating = false;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (cooldownLeft <= 0 || env.poolHealths[(int) targetEffect1][(int) targetEffect2] <= 0)
			use(env, user, target);
		else
		{
			// effects
			env.otherDebris((targetEffect1 + 0.5) * squareSize, (targetEffect2 + 0.5) * squareSize, getElementNum(), "pool heal", frameNum);
			env.poolHealths[(int) targetEffect1][(int) targetEffect2] += points;
			if (env.poolHealths[(int) targetEffect1][(int) targetEffect2] >= 100)
				env.poolHealths[(int) targetEffect1][(int) targetEffect2] = 100;
			// Might be resource-costly:
			env.updatePools();
			//
			user.mana -= costPerSecond * deltaTime;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.rangeArea = new Area();

		for (int i = (int) (player.x - range); i < (int) (player.x + range); i += squareSize)
			for (int j = (int) (player.y - range); j < (int) (player.y + range); j += squareSize)
				if (Math.pow(player.x - i / squareSize * squareSize - 0.5 * squareSize, 2) + Math.pow(player.y - j / squareSize * squareSize - 0.5 * squareSize, 2) <= range * range)
					player.rangeArea.add(new Area(new Rectangle2D.Double(i / squareSize * squareSize, j / squareSize * squareSize, squareSize, squareSize)));

		// double angle = Math.atan2(player.target.y - player.y, player.target.x - player.x);

		int gridX = player.target.x / squareSize, gridY = player.target.y / squareSize;
		player.targetType = "createInGrid";
		if (!player.maintaining)
		{
			// if (Methods.DistancePow2(new Point((int) (player.x), (int) (player.y)), new Point(gridX * squareSize + squareSize / 2, gridY * squareSize + squareSize / 2)) > range * range)
			// {
			// player.target.x = (int) (player.x + Math.cos(angle) * (range / 2));
			// player.target.y = (int) (player.y + Math.sin(angle) * (range / 2));
			// gridX = player.target.x / squareSize;
			// gridY = player.target.y / squareSize;
			// }
			boolean canCreate = true;
			if (env.poolTypes[gridX][gridY] != -1)
				if (env.poolTypes[gridX][gridY] != getElementNum() || (env.poolTypes[gridX][gridY] == getElementNum() && env.poolHealths[gridX][gridY] >= 100))
					canCreate = false;
			// stop creating pool if it collides with someone
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
