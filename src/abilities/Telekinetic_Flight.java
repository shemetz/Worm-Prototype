package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Telekinetic_Flight extends _FlightAbility
{
	boolean gaveBonuses;

	public Telekinetic_Flight(int p)
	{
		super("Telekinetic Flight", p);
		cost = -1;
		costPerSecond = -1;
		costType = CostType.NONE;
		instant = true;
		gaveBonuses = false;
	}

	public void updateStats()
	{
		cooldown = 1;
		flySpeed = 900 * LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (!on && user.stamina > 2 && !user.prone && cooldownLeft == 0)
		{
			on = true;
			if (user.z == 0)
				user.z += 0.2;
			user.flySpeed = flySpeed;
			cooldownLeft = 0.5; // constant activation cooldown - to fix keys being stuck, etc.

			if (!gaveBonuses)
			{
				gaveBonuses = true;
				user.STRENGTH += 2;
				user.pushbackResistance += 0.5 * (1 - user.pushbackResistance); // +50% pushback immunity
				user.evasion += 0.2 * (1 - user.evasion); // +20% evasion
			}
		}
		else if (on && cooldownLeft == 0)
		{
			on = false;
			cooldownLeft = cooldown;
			user.flySpeed = -1;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}
}
