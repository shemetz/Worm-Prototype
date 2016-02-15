package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Toughness_III extends Ability
{
	public Toughness_III(int p)
	{
		super("Toughness III", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.life *= val * 100 * level / user.maxLife + 1;
		user.maxLife += val * 100 * level;
		on = !on;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, null);
	}
}
