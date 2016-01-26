package abilities;

import effects.Healed;
import mainClasses.Effect;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Heal_I extends ApplyEffect
{

	public Heal_I(int p)
	{
		super("Heal I", p, ApplyEffect.targetTypes.OTHER, VisualEffect.Type.HEAL);
		cost = 0;
		costPerSecond = 1;
		costType = "mana";
		cooldown = 0;
		range = 50 * level;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = true;
		instant = true;
	}

	public boolean viableTarget(Person p, Person user)
	{
		if (!defaultViableTarget(p, user))
			return false;
		if (p.life / p.maxLife == 1)
			return false;
		return true;
	}

	public Effect effect()
	{
		return new Healed(2 * level, this);
	}
}
