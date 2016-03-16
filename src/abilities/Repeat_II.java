package abilities;

import java.awt.Point;
import java.util.List;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Repeat_II extends _LoopAbility
{
	public Repeat_II(int p)
	{
		super("Repeat II", p, Targeting.TARGETED);

		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		instant = false;

		position = true;
		state = true;
	}

	public void updateStats()
	{
		cost = 6 - 0.5 * LEVEL;
		cooldown = 2;
		range = 500;
		duration = LEVEL;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		// effect
		VisualEffect vfx = new VisualEffect();
		List<Person> targets = getTargets(env, user, target);
		if (!targets.isEmpty())
		{
			vfx.p1 = targets.get(0).Point();
			vfx.duration = 1;
			vfx.timeLeft = 1;
			vfx.type = VisualEffect.Type.TELEPORT;
			vfx.image = targets.get(0).image;
			vfx.angle = targets.get(0).rotation;
		}
		double prevAmount = this.cooldownLeft;

		super.use(env, user, target);

		VisualEffect vfx2 = new VisualEffect();
		if (!targets.isEmpty())
		{
			vfx2.p1 = targets.get(0).Point();
			vfx2.duration = 1;
			vfx2.timeLeft = 1;
			vfx2.type = VisualEffect.Type.STATE_LOOP;
			vfx2.image = targets.get(0).image;
			vfx2.angle = targets.get(0).rotation;
		}
		if (prevAmount != this.cooldownLeft) // clutch way of checking if ability was used
		{
			env.visualEffects.add(vfx);
			env.visualEffects.add(vfx2);
		}
	}
}
