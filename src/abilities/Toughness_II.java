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

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.life *= val * 40 * level / user.maxLife + 1;
		user.maxLife += val * 40 * level;
		on = !on;
	}
}
