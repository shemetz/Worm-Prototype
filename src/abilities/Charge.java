package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Charge extends _PassiveAbility
{

	public double minimumVelocityPow2 = Math.pow(400,2);

	public Charge(int p)
	{
		super("Charge", p);

		damage = 2 * level;
		pushback = 1 + level * 1;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.STRENGTH += level * val;
	}
}
