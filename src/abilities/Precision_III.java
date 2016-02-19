package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Precision_III extends _PassiveAbility
{
	double oldValue;

	public Precision_III(int p)
	{
		super("Precision III", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.DEXTERITY += 3 * val * level;
		if (on) // activate
		{
			oldValue = user.accuracy;
			user.accuracy = 1;
		}
		else
			user.accuracy = oldValue;
		user.updateAccuracy();
	}
}
