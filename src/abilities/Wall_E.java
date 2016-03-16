package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Wall_E extends _GridTargetingAbility
{
	// Hahahahahahahaha
	//
	// ...
	//
	// "Wall E"
	//

	public Wall_E(String elementName, int p)
	{
		super("Wall <" + elementName + ">", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		if (target == null)
			return;
		/*
		 * Create a wall. Destroys any pools in that area.
		 */
		int gridX = target.x / squareSize, gridY = target.y / squareSize;
		boolean repairingWall = env.wallTypes[gridX][gridY] == elementNum && env.wallHealths[gridX][gridY] < 100 && 0.3 <= user.mana;
		if (!on && !user.maintaining && (cost <= user.mana || repairingWall))
		{
			boolean canCreate = true;
			if (!repairingWall)
			{
				// stop creating wall if there already is a different wall there
				if (env.wallTypes[gridX][gridY] != -1)
					if (env.wallTypes[gridX][gridY] != elementNum || (env.wallTypes[gridX][gridY] == elementNum && env.wallHealths[gridX][gridY] >= 100))
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
				targetGridX = gridX;
				targetGridY = gridY;
				canBuildInTarget = true; // able
				env.addWall(gridX, gridY, elementNum, false);
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
				canBuildInTarget = false; // unable
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
		frameNum++;
		if (cooldownLeft <= 0 || env.wallHealths[(int) targetGridX][(int) targetGridY] <= 0)
			use(env, user, target);
		else
		{
			// effects
			env.otherDebris((targetGridX + 0.5) * squareSize, (targetGridY + 0.5) * squareSize, elementNum, "wall heal", frameNum);
			env.wallHealths[(int) targetGridX][(int) targetGridY] = (int) Math.max(Math.max((1 - cooldownLeft / cooldown) * 100, 1), env.wallHealths[(int) targetGridX][(int) targetGridY]); // make sure you aren't decreasing current health
			user.mana -= costPerSecond * deltaTime;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.CREATE_IN_GRID;
		UPT(env, player);
		if (!player.maintaining)
		{
			int gridX = player.target.x / squareSize, gridY = player.target.y / squareSize;
			if (env.wallTypes[gridX][gridY] != -1)
				if (env.wallTypes[gridX][gridY] != elementNum || (env.wallTypes[gridX][gridY] == elementNum && env.wallHealths[gridX][gridY] >= 100))
					canBuildInTarget = false;
		}
	}
}
