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

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.life *= val * 20 * level / user.maxLife + 1;
		user.maxLife += val * 20 * level;
		on = !on;
	}
}
