package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Evasion_III extends _PassiveAbility
{
	double amount2;
	double originalEvasion;

	public Evasion_III(int p)
	{
		super("Evasion II", p);
	}

	public void updateStats()
	{
		amount = 1;
		amount2 = 3 * LEVEL;
		originalEvasion = 1;
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		double temp = originalEvasion;
		originalEvasion = user.evasion;
		user.evasion = temp;
		user.DEXTERITY += val * amount2;
		on = !on;
	}
}
