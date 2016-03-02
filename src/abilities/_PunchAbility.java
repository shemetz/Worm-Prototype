package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class _PunchAbility extends _PassiveAbility
{

	public _PunchAbility(String s, int p)
	{
		super(s, p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		if (on)
			user.punchAffectingAbilities.add(this);
		else
			user.punchAffectingAbilities.remove(this);
	}
}
