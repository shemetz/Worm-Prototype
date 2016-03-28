package abilities;

import effects.Time_Slowed;
import mainClasses.Effect;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Slow_Target extends _ApplyEffect
{
	public Slow_Target(int p)
	{
		super("Slow Target", p, _ApplyEffect.TargetType.TARGETED, VisualEffect.Type.NO);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		stopsMovement = false;
		maintainable = false;
		instant = false;
	}

	public void updateStats()
	{
		cost = 6 - LEVEL / 2;
		cooldown = 3;
		range = 500;
		duration = LEVEL * 2;
		
	}

	public Effect effect()
	{
		return new Time_Slowed(duration, this);
	}

	public boolean viableTarget(Person p, Person user)
	{
		return true;
	}
}
