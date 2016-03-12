package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Explosive_Fists extends _PunchAbility
{

	Explosion_Resistance givenAbility;

	public Explosive_Fists(int p)
	{
		super("Explosive Fists", p);

		rangeType = Ability.RangeType.EXPLOSION;
		radius = 200;
		damage = level;
		pushback = level * 8;

		givenAbility = (Explosion_Resistance) Ability.ability("Explosion Resistance", 0);
	}

	public void use(Environment env, Person user, Point target)
	{
		super.use(env, user, target);
		if (on)
			user.abilities.add(givenAbility);
		else
			user.abilities.remove(givenAbility);
	}
}
