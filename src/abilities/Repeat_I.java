package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Repeat_I extends _LoopAbility
{
	public Repeat_I(int p)
	{
		super("Repeat I", p, Targeting.SELF);

		costType = CostType.MANA;
		rangeType = RangeType.NONE;
		instant = false;

		position = true;
		state = true;
	}

	public void updateStats()
	{
		cost = 4 - 0.5 * level;
		cooldown = 2;
		range = 0;
		duration = level;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		// effect
		VisualEffect vfx = new VisualEffect();
		vfx.p1 = user.Point();
		vfx.duration = 1;
		vfx.timeLeft = 1;
		vfx.type = VisualEffect.Type.TELEPORT;
		vfx.image = user.image;
		vfx.angle = user.rotation;
		double prevAmount = this.cooldownLeft;

		super.use(env, user, target);

		VisualEffect vfx2 = new VisualEffect();
		vfx2.p1 = user.Point();
		vfx2.duration = 1;
		vfx2.timeLeft = 1;
		vfx2.type = VisualEffect.Type.STATE_LOOP;
		vfx2.image = user.image;
		vfx2.angle = user.rotation;

		if (prevAmount != this.cooldownLeft) // clutch way of checking if ability was used
		{
			env.visualEffects.add(vfx);
			env.visualEffects.add(vfx2);
		}
	}
}
