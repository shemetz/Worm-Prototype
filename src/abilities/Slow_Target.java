package abilities;

import effects.Time_Slowed;
import mainClasses.Effect;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Slow_Target extends _ApplyEffect
{
	public Slow_Target(int p)
	{
		super("Slow Target", p, _ApplyEffect.targetTypes.TARGETED, VisualEffect.Type.NO);
		cost = 6 - level / 2;
		costPerSecond = 1;
		costType = CostType.MANA;
		cooldown = 3;
		range = 500;
		rangeType = RangeType.CIRCLE_AREA;
		stopsMovement = false;
		maintainable = false;
		instant = false;
	}

	public Effect effect()
	{
		return new Time_Slowed(level * 2, this);
	}

	public boolean viableTarget(Person p, Person user)
	{
		return true;
	}
}
