package abilities;

import effects.Healed;
import mainClasses.Effect;

public class Heal_I extends ApplyEffect
{

	public Heal_I(int p)
	{
		super("Heal I", p, ApplyEffect.targetTypes.OTHER, 3);
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

	public Effect effect()
	{
		return new Healed(2*level, this);
	}
}
