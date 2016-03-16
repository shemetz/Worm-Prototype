package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Strength_II extends _PassiveAbility
{

	double fitnessAmount;

	public Strength_II(int p)
	{
		super("Strength II", p);
	}

	public void updateStats()
	{
		amount = 2 * LEVEL;
		fitnessAmount = 2;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		int v = on ? -1 : 1;
		user.STRENGTH += v * amount;
		user.FITNESS += v * fitnessAmount;
		on = !on;
	}
}
