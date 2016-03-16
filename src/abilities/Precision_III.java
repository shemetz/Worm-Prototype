package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Precision_III extends _PassiveAbility
{
	double oldValue;
	double dexBonus;

	public Precision_III(int p)
	{
		super("Precision III", p);
	}

	public void updateStats()
	{
		dexBonus = 3 * LEVEL;
		amount = 1;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.DEXTERITY += val * dexBonus;
		if (on) // activate
		{
			oldValue = user.accuracy;
			user.accuracy = amount;
		}
		else
			user.accuracy = oldValue;
		user.updateAccuracy();
	}
}
