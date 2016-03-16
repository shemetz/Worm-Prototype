package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Charge extends _PassiveAbility
{

	double extraStrength;
	public double minimumVelocityPow2 = Math.pow(350, 2);

	public Charge(int p)
	{
		super("Charge", p);
	}

	public void updateStats()
	{
		damage = 2 * LEVEL;
		pushback = 1 + LEVEL * 1;
		extraStrength = LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.STRENGTH += extraStrength * val;
	}
}
