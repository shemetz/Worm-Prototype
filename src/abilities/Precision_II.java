package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Precision_II extends _PassiveAbility
{
	double dexBonus;

	public Precision_II(int p)
	{
		super("Precision II", p);
	}

	public void updateStats()
	{
		dexBonus = 2 * level;
		amount = Math.pow(0.8, level);
		
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
		user.updateAccuracy();
	}
}
