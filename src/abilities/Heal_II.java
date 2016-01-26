package abilities;

import effects.Healed;
import mainClasses.Effect;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Heal_II extends ApplyEffect
{

	public Heal_II(int p)
	{
		super("Heal II", p, ApplyEffect.targetTypes.AREA, VisualEffect.Type.HEAL);
		cost = 0;
		costPerSecond = 3;
		costType = "mana";
		cooldown = 0;
		range = 100 * points;
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
		return new Healed(4 * level, this);
	}
}
