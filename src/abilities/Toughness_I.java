package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Toughness_I extends _PassiveAbility
{
	public Toughness_I(int p)
	{
		super("Toughness I", p);
	}

	public void updateStats()
	{
		amount = 20 * LEVEL;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.life *= val * amount / user.maxLife + 1;
		user.maxLife += val * amount;
		on = !on;
	}
}
