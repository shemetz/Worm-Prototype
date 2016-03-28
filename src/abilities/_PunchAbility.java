package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

/**
 * An ability that is applied to a punch when using {@link Punch}.
 * 
 * @author Itamar
 *
 */
public class _PunchAbility extends _PassiveAbility
{

	public _PunchAbility(String s, int p)
	{
		super(s, p);
	}

	/**
	 * Adds/removes the ability to/from the {@link Person#punchAffectingAbilities} list.
	 */
	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		if (on)
			user.punchAffectingAbilities.add(this);
		else
			user.punchAffectingAbilities.remove(this);
	}
}
