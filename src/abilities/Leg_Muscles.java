package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Leg_Muscles extends Ability
{

	public Leg_Muscles(int p)
	{
		super("Leg Muscles", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.runSpeed += val * 50 * level;
		user.runAccel += val * 1000 * level;
		on = !on;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, null);
	}
}
