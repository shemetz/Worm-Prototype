package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Toughness_II extends _PassiveAbility
{
	public Toughness_II(int p)
	{
		super("Toughness II", p);
	}

	public void updateStats()
	{
		amount = 40 * level;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.life *= val * amount / user.maxLife + 1;
		user.maxLife += val * amount;
		on = !on;
	}
}
