package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Chronobiology extends Ability
{

	double amount;
	boolean initialChange = false;

	public Chronobiology(int p)
	{
		super("Chronobiology", p);
		cost = 3; // to switch
		costType = "mana";
		instant = true;

		amount = 1 + 0.2 * level;
		on = true; // on = fast, off = slow
	}

	public void use(Environment env, Person user, Point target)
	{
		if (user.mana >= cost)
		{
			user.mana -= cost;
			on = !on;
			if (on)
				user.timeEffect *= amount * amount;
			else
				user.timeEffect /= amount * amount;
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (!initialChange)
		{
			user.timeEffect *= amount;
			initialChange = true;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
	}
}
