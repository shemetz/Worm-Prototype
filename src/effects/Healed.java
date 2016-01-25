package effects;

import mainClasses.Effect;
import mainClasses.Person;

public class Healed extends Effect
{
	public Healed(int strength1)
	{
		super("Healed", 0, strength1);
		stackable = true;
	}

	public void apply(Person target)
	{
		super.apply(target);
		target.lifeRegen += 2 * strength;
	}

	public void remove(Person target)
	{
		super.remove(target);
		target.lifeRegen -= 2 * strength;
	}
}
