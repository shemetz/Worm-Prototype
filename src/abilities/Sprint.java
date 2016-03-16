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
		costType = CostType.STAMINA;
		rangeType = RangeType.NONE;
		maintainable = true;
		instant = true;
		natural = true;
	}

	public void updateStats()
	{
		costPerSecond = 3 - 0.3 * LEVEL;
		
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, user.target);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		user.maintaining = !user.maintaining;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{

	}
}
