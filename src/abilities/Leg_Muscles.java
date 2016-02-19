package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Leg_Muscles extends _PassiveAbility
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
}
