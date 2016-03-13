package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Strength_I extends _PassiveAbility
{

	double fitnessAmount;

	public Strength_I(int p)
	{
		super("Strength I", p);
	}

	public void updateStats()
	{
		amount = level;
		fitnessAmount = 1;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		int v = on ? -1 : 1;
		user.STRENGTH += v * amount;
		user.FITNESS += v * 1;
		on = !on;
	}
}
