package effects;

import mainClasses.Effect;
import mainClasses.Person;

public class Burning extends Effect
{
	public Burning(int strength1)
	{
		super("Burning", -1, strength1);
		stackable = false;
	}

	public void apply(Person target)
	{
		super.apply(target);
		target.panic = true;
	}

	public void remove(Person target)
	{
		super.remove(target);
		target.panic = false;
	}
}
