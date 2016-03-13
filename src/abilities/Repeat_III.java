package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Repeat_III extends _LoopAbility
{
	public Repeat_III(int p)
	{
		super("Repeat III", p, Targeting.AREA);

		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		instant = false;

		position = true;
		state = true;
	}

	public void updateStats()
	{
		cost = 8 - 0.5 * level;
		cooldown = 2;
		range = 1500;
		duration = level;
		radius = 250;
	}

	public void use(Environment env, Person user, Point target)
	{
		// effect
		List<Person> targets = getTargets(env, user, target);
		List<VisualEffect> vfxs = new ArrayList<VisualEffect>();
		for (Person p : targets)
		{
			VisualEffect vfx = new VisualEffect();
			vfx.p1 = p.Point();
			vfx.duration = 1;
			vfx.timeLeft = 1;
			vfx.type = VisualEffect.Type.TELEPORT;
			vfx.image = p.image;
			vfx.angle = p.rotation;
			vfxs.add(vfx);
		}
		double prevAmount = this.cooldownLeft;

		super.use(env, user, target);

		for (Person p : targets)
		{
			VisualEffect vfx = new VisualEffect();
			vfx.p1 = p.Point();
			vfx.duration = 1;
			vfx.timeLeft = 1;
			vfx.type = VisualEffect.Type.STATE_LOOP;
			vfx.image = p.image;
			vfx.angle = p.rotation;
			vfxs.add(vfx);
		}

		if (prevAmount != this.cooldownLeft) // clutch way of checking if ability was used
			for (VisualEffect vfx : vfxs)
				env.visualEffects.add(vfx);
	}
}
