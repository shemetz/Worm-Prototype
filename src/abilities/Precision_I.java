package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Precision_I extends _PassiveAbility
{

	double dexBonus;

	public Precision_I(int p)
	{
		super("Precision I", p);
	}

	public void updateStats()
	{
		dexBonus = 1 * level;
		amount = Math.pow(0.9, level);
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.DEXTERITY += val * dexBonus;
		if (on) // activate
			user.accuracy = 1 - (1 - user.accuracy) * amount;
		else // deactivate
			user.accuracy = 1 - (1 - user.accuracy) / amount;
		// a = 1-(1-b)*(0.9^level)
		// b = 1-(1-a)/(0.9^level)
		user.updateAccuracy();
	}
}
