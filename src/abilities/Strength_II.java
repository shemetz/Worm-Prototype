package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Strength_II extends _PassiveAbility
{

	public Strength_II(int p)
	{
		super("Strength II", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		int v = on ? -1 : 1;
		user.STRENGTH += 2 * v * level;
		user.FITNESS += v * 2;
		on = !on;
	}
}
