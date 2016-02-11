package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.VisualEffect;

public class Undo_I extends _LoopAbility
{
	public Undo_I(int p)
	{
		super("Undo I", p, Targeting.SELF);

		cost = 4 - 0.5 * level;
		costType = CostType.MANA;
		cooldown = 2;
		range = 0;
		rangeType = RangeType.NONE;
		instant = false;

		amount = level;
		position = false;
		state = true;
	}

	public void use(Environment env, Person user, Point target)
	{
		// effect
		VisualEffect vfx = new VisualEffect();
		vfx.p1 = user.Point();
		vfx.duration = 1;
		vfx.timeLeft = 1;
		vfx.type = VisualEffect.Type.STATE_LOOP;
		vfx.image = user.image;
		vfx.angle = user.rotation;
		double prevAmount = this.cooldownLeft;

		super.use(env, user, target);

		if (prevAmount != this.cooldownLeft) // clutch way of checking if ability was used
			env.visualEffects.add(vfx);
	}
}
