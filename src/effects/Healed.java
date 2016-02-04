package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Healed extends Effect
{
	public Healed(double lifeRegenBuff, Ability creatorAbility1)
	{
		super("Healed", -1, lifeRegenBuff, creatorAbility1);
		stackable = true;
	}

	public void apply(Person target)
	{
		target.lifeRegen += 2 * strength;
	}

	public void unapply(Person target)
	{
		target.lifeRegen -= 2 * strength;
	}
}
