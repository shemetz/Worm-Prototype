package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Precision_I extends Ability
{

	public Precision_I(int p)
	{
		super("Precision I", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.DEXTERITY += val * level;
		if (on) // activate
			user.accuracy = 1 - (1 - user.accuracy) * Math.pow(0.9, level);
		else // deactivate
			user.accuracy = 1 - (1 - user.accuracy) / Math.pow(0.9, level);
		//a = 1-(1-b)*(0.9^level)
		//b = 1-(1-a)/(0.9^level)
		user.updateAccuracy();
	}
}
