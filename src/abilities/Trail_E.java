package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Ball;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Person;
import mainClasses.Player;

public class Trail_E extends Ability
{
	public static enum Type
	{
		POOL, WALL, BALL
	};

	Type type;
	Point lastPlace = null;
	final int squareSize = 96;

	public Trail_E(String element, int p)
	{
		super("Trail <" + element + ">", p);
		costType = CostType.MANA;
		instant = true;
	}

	public void updateStats()
	{
		cooldown = 0; // once per frame
		cost = Math.max(0, 0.7 - 0.1 * level);

		switch (elementNum)
		{
		case 1: // Water
		case 7: // Acid
		case 8: // Lava
		case 9: // Flesh
			type = Type.POOL;
			break;
		case 4: // Metal
		case 5: // Ice
		case 10: // Earth
		case 11: // Plant
			type = Type.WALL;
			break;
		case 0: // Fire
		case 2: // Wind
		case 3: // Electricity
		case 6: // Energy
			type = Type.BALL;
			damage = 0.6 * level * Ability.elementalAttackNumbers[elementNum][0];
			pushback = 0.6 * level * Ability.elementalAttackNumbers[elementNum][1] + 1;
			break;
		default:
			MAIN.errorMessage("You made a bear! Undo it, undo it!");
			break;
		}
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		if (on)
			lastPlace = new Point((int) (user.x / squareSize), (int) (user.y / squareSize));
	}

	double debrisTimer = 0;

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (user.mana >= cost)
		{
			if (user.x + user.radius < lastPlace.x * squareSize || user.y + user.radius < lastPlace.y * squareSize || user.x - user.radius > (lastPlace.x + 1) * squareSize
					|| user.y - user.radius > (lastPlace.y + 1) * squareSize)
			{
				switch (type)
				{
				case POOL:
					// if user no longer intersects last place
					env.addPool(lastPlace.x, lastPlace.y, elementNum, true);
					break;
				case WALL:
					// if user no longer intersects last place
					env.addWall(lastPlace.x, lastPlace.y, elementNum, true);
					break;
				case BALL:
					// if user no longer intersects last place
					Ball b = new Ball((lastPlace.x + 0.5) * squareSize, (lastPlace.y + 0.5) * squareSize, user.z + 0.9, elementNum, damage, pushback, user.angle() + Math.PI, user, 100);
					env.balls.add(b);
					break;
				default:
					MAIN.errorMessage("WERTDFGHJNBVCXSWQAZ");
					break;
				}
				lastPlace = new Point((int) (user.x / squareSize), (int) (user.y / squareSize));
				user.mana -= cost;
			}
			debrisTimer += deltaTime;
			if (debrisTimer > 0.5)
			{
				debrisTimer = 0;
				env.otherDebris(user.x, user.y, elementNum, "trail", 0);
			}
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		;
	}
}
