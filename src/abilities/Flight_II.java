package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Flight_II extends Ability
{

	public Flight_II(int p)
	{
		super("Flight II", p);
		costPerSecond = 0.4;
		costType = "stamina";
		cooldown = 1;
		cost = 0;
		instant = true;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (!on && user.stamina > 2 && !user.prone && cooldownLeft == 0)
		{
			on = true;
			if (user.z == 0)
				user.z += 0.1;
			user.flySpeed = 300 * points; // 800 to 1200 pixels per second
			cooldownLeft = 0.5; // constant activation cooldown - to fix keys being stuck, etc.
		} else if (on && cooldownLeft == 0)
		{
			on = false;
			cooldownLeft = cooldown;
			user.flySpeed = -1;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		user.stamina -= deltaTime * costPerSecond;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "";
		player.target = new Point(-1, -1);
	}
}
