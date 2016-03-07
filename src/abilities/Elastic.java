package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Elastic extends _PassiveAbility
{

	public double minimumVelocityPow2 = Math.pow(500,2);

	public Elastic(int p)
	{
		super("Elastic", p);
		damage = level * 1;
		pushback = level * 2;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.STRENGTH += level * val;
	}
}
