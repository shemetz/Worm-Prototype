package abilities;

import java.awt.Point;
import java.util.List;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Retrace_II extends _LoopAbility
{
	public Retrace_II(int p)
	{
		super("Retrace II", p, "TARGETED");

		cost = 6 - 0.5 * level;
		costType = "Mana";
		cooldown = 2;
		range = 500;
		rangeType = "Circle area";
		instant = false;

		amount = level;
		position = true;
		state = false;
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

		if (prevAmount != this.cooldownLeft) // clutch way of checking if ability was used
			env.visualEffects.add(vfx);
	}
}
