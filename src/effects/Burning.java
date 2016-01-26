package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Burning extends Effect
{
	public Burning(int strength1, Ability CA)
	{
		super("Burning", -1, strength1, CA);
		stackable = false;
	}

	public void apply(Person target)
	{
		target.panic = true;
	}

	public void unapply(Person target)
	{
		target.panic = false;
	}
}
