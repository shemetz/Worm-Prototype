package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Strength_III extends _PassiveAbility
{

	public Strength_III(int p)
	{
		super("Strength III", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		int v = on ? -1 : 1;
		user.STRENGTH += v * Math.pow(2, level);
		user.FITNESS += 0.5 * v * level;
		on = !on;
	}

}
