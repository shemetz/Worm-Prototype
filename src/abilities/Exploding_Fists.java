package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Exploding_Fists extends _PunchAbility
{

	public Exploding_Fists(int p)
	{
		super("Exploding Fists", p);

		damage = level * 4;
		pushback = level * 8;
	}

	public void use(Environment env, Person user, Point target)
	{
		super.use(env, user, target);
		if (on)
			user.abilities.add(Ability.ability("Explosion Resistance", 0));
	}
}
