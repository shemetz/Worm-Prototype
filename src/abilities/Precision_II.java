package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Precision_II extends _PassiveAbility
{

	public Precision_II(int p)
	{
		super("Precision II", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.DEXTERITY += 2 * val * level;
		if (on) // activate
			user.accuracy = 1 - (1 - user.accuracy) * Math.pow(0.8, level);
		else // deactivate
			user.accuracy = 1 - (1 - user.accuracy) / Math.pow(0.8, level);
		user.updateAccuracy();
	}
}
