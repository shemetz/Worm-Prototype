package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Explosion_Resistance extends _PassiveAbility
{
	public Explosion_Resistance(int p)
	{
		super("Explosion Resistance", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
