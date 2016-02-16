package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Sprint extends Ability
{

	public Sprint(int p)
	{
		super("Sprint", p);
		cost = 0;
		costType = CostType.STAMINA;
		cooldown = 0;
		rangeType = RangeType.NONE;
		maintainable = true;
		instant = true;
		costPerSecond = 3 - 0.3 * level;
		natural = true;
	}
	
	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, user.target);
	}

	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft == 0 || on)
		{
			on = !on;
			user.maintaining = !user.maintaining;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{

	}
}
