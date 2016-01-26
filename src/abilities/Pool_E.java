package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Pool_E extends GridTargetingAbility
{

	public Pool_E(String elementName, int p)
	{
		super("Pool <" + elementName + ">", p);
		// reduced cost normal cost minus 1.5
	}

	public void use(Environment env, Person user, Point target)
	{
		if (target == null)
			return;
		/*
		 * Create a pool. The cost of creating a pool is reduced if there's an adjacent pool.
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
				if (env.poolTypes[gridX][gridY] != getElementNum() || (env.poolTypes[gridX][gridY] == getElementNum() && env.poolHealths[gridX][gridY] >= 100) || env.wallTypes[gridX][gridY] != -1)
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
				targetGridX = gridX;
				targetGridY = gridY;
				canBuildInTarget = true; // able
				env.addPool(gridX, gridY, getElementNum(), false);
				env.poolHealths[gridX][gridY] = Math.max(env.poolHealths[gridX][gridY], 1);
				user.maintaining = true;
				user.notAnimating = true;
				cooldownLeft = cooldown;
				on = true;
				user.switchAnimation(2);
			} else
				canBuildInTarget = false; // unable
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
		frameNum++;
		if (cooldownLeft <= 0 || env.poolHealths[(int) targetGridX][(int) targetGridY] <= 0)
			use(env, user, target);
		else
		{
			// effects
			env.otherDebris((targetGridX + 0.5) * squareSize, (targetGridY + 0.5) * squareSize, getElementNum(), "pool heal", frameNum);
			env.poolHealths[(int) targetGridX][(int) targetGridY] += level;
			if (env.poolHealths[(int) targetGridX][(int) targetGridY] >= 100)
				env.poolHealths[(int) targetGridX][(int) targetGridY] = 100;
			// Might be resource-costly:
			env.updatePools();
			//
			user.mana -= costPerSecond * deltaTime;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "createInGrid";
		UPT(env, player);
		if (!player.maintaining)
		{
			int gridX = player.target.x / squareSize, gridY = player.target.y / squareSize;
			if (env.poolTypes[gridX][gridY] != -1)
				if (env.poolTypes[gridX][gridY] != getElementNum() || (env.poolTypes[gridX][gridY] == getElementNum() && env.poolHealths[gridX][gridY] >= 100) || env.wallTypes[gridX][gridY] != -1)
					canBuildInTarget = false;
		}
	}
}
