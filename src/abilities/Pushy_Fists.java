package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Pushy_Fists extends _PunchAbility
{

	public Pushy_Fists(int p)
	{
		super("Pushy Fists", p);
	}

	@Override
	public void use(Environment env, Person user, Point target)
	{
		super.use(env, user, target);
		user.STRENGTH += 0.5 * level * (on ? 1 : -1);
		user.pushbackResistance = 1 - (1 - user.pushbackResistance) * (on ? 0.5 : 2);
	}

}
