package abilities;

import effects.Healed;
import mainClasses.Effect;

public class Heal_II extends ApplyEffect
{

	public Heal_II(int p)
	{
		super("Heal II", p, ApplyEffect.targetTypes.AREA, 3);
		cost = 0;
		costPerSecond = 3;
		costType = "mana";
		cooldown = 0;
		range = 100 * level;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = true;
		instant = true;
	}

	public Effect effect()
	{
		return new Healed(4*level, this);
	}
}
