package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Evasion_I extends _PassiveAbility
{
	double amount2;

	public Evasion_I(int p)
	{
		super("Evasion I", p);
	}

	public void updateStats()
	{
		amount = Math.pow(0.9, LEVEL);
		amount2 = LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.evasion = 1 - Math.pow(amount, val) * (1 - (user.evasion));
		user.DEXTERITY += val * amount2;
		on = !on;
	}
}
