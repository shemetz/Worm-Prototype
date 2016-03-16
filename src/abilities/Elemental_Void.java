package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Debris;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Elemental_Void extends Ability
{

	final int squareSize = 96;
	double timer;

	public Elemental_Void(int p)
	{
		super("Elemental Void", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		instant = true;

		timer = 0;
	}

	public void updateStats()
	{
		cooldown = 0.2;
		range = LEVEL * 100;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0)
		{
			on = !on;
			cooldownLeft = cooldown;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		deltaTime *= user.timeEffect;
		timer += deltaTime;
		damage = (int) (50000000 * deltaTime);
		if (timer > 0.25) // every quarter second
		{
			timer -= 0.25;
			int minX = Math.max(0, (int) ((user.x - range) / squareSize));
			int maxX = Math.min(env.width - 1, (int) ((user.x + range) / squareSize));
			int minY = Math.max(0, (int) ((user.y - range) / squareSize));
			int maxY = Math.min(env.width - 1, (int) ((user.y + range) / squareSize));
			for (int x = minX; x <= maxX; x++)
				for (int y = minY; y <= maxY; y++)
				{
					int centerX = (int) ((x + Math.random()) * squareSize);
					int centerY = (int) ((y + Math.random()) * squareSize);
					double distancePow2 = Methods.DistancePow2(centerX, centerY, user.x, user.y);
					if (distancePow2 <= range * range)
					{
						double speed = 20 * damage / distancePow2; // speed of debris
						if (env.wallTypes[x][y] != -2) // not map edge walls
							if (env.wallHealths[x][y] > 0)
							{
								env.wallHealths[x][y] -= damage / distancePow2;
								env.debris.add(new Debris(centerX, centerY, 1, Math.atan2(user.y - centerY, user.x - centerX) + Math.PI / 2, env.getWallDebrisType(env.wallTypes[x][y]), speed));
								if (env.wallHealths[x][y] <= 0)
									env.destroyWall(x, y);
								env.connectWall(x, y);
							}
						if (env.poolHealths[x][y] > 0)
						{
							env.poolHealths[x][y] -= damage / distancePow2;
							env.debris.add(new Debris(centerX, centerY, 1, Math.atan2(user.y - centerY, user.x - centerX) + Math.PI / 2, env.getPoolDebrisType(env.poolTypes[x][y]), speed));
							if (env.poolHealths[x][y] <= 0)
								env.destroyPool(x, y);
						}
					}
				}
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		on = false;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
		player.target = new Point(-1, -1);
	}
}
