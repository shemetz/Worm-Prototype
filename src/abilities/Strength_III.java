package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Strength_III extends _PassiveAbility
{
	double fitnessAmount;

	public Strength_III(int p)
	{
		super("Strength III", p);

	}

	public void updateStats()
	{
		amount = Math.pow(2, LEVEL);
		fitnessAmount = 0.5 * LEVEL;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		int v = on ? -1 : 1;
		user.STRENGTH += v * amount;
		user.FITNESS += v * fitnessAmount;
		on = !on;
	}

}
