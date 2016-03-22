package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Evasion_II extends _PassiveAbility
{
	double amount2;

	public Evasion_II(int p)
	{
		super("Evasion II", p);
	}

	public void updateStats()
	{
		amount = Math.pow(0.8, LEVEL);
		amount2 = 2*LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.evasion = 1 - Math.pow(amount, val) * (1 - (user.evasion));
		user.DEXTERITY += val * amount2;
		on = !on;
	}
}
