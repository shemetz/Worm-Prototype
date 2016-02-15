package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.VisualEffect;
import mainResourcesPackage.SoundEffect;

public class Blink extends TeleportAbility
{

	public Blink(int p)
	{
		super("Blink", p);
		cost = 1 + (double) (level) / 3;
		costType = CostType.MANA;
		cooldown = 0.1 + (double) (level) / 4;
		range = level * 100;
		rangeType = RangeType.EXACT_RANGE; // maybe change it to up-to range?

		sounds.add(new SoundEffect("Blink_success.wav"));
		sounds.add(new SoundEffect("Blink_fail.wav"));
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		/*
		 * Teleport ahead to target direction
		 * 
		 * visual effect types:
		 * 
		 * 1 = just blinked, and there's an effect.
		 * 
		 * 2 = failed to blink
		 */
		if (cooldownLeft > 0 || cost > user.mana)
			return;

		// if not maintaining a power, the user will rotate to fit the teleportation.
		if (!user.maintaining)
			user.rotation = angle;

		user.x += range * Math.cos(angle);
		user.y += range * Math.sin(angle);
		if (checkIfAvailable(user.x, user.y, user.z, env, user)) // managed to teleport
		{
			user.mana -= cost;
			cooldownLeft = cooldown;
			final int numOfLines = 5;
			for (int j = 0; j < numOfLines; j++)
			{
				VisualEffect eff = new VisualEffect();
				eff.type = VisualEffect.Type.BLINK_SUCCESS;
				eff.duration = 0.4;
				eff.timeLeft = eff.duration;
				eff.p1p2variations = new Point(user.radius, user.radius);
				eff.p1 = new Point((int) (user.x - range * Math.cos(angle)), (int) (user.y - range * Math.sin(angle)));
				eff.p2 = new Point((int) (user.x), (int) (user.y));
				eff.onTop = false;
				env.visualEffects.add(eff);
			}
			// SFX
			sounds.get(0).play();
		}
		else
		// tried to blink into something
		{
			user.x -= range * Math.cos(angle);
			user.y -= range * Math.sin(angle);
			final int numOfLines = 3;
			for (int j = 0; j < numOfLines; j++)
			{
				VisualEffect eff = new VisualEffect();
				eff.type = VisualEffect.Type.BLINK_FAIL;
				eff.duration = 0.3;
				eff.timeLeft = eff.duration;
				eff.p1p2variations = new Point(user.radius, user.radius);
				eff.p1 = new Point((int) (user.x + range * Math.cos(angle)), (int) (user.y + range * Math.sin(angle)));
				eff.p2 = new Point((int) (user.x), (int) (user.y));
				eff.onTop = true;
				env.visualEffects.add(eff);
			}

			// SFX
			sounds.get(1).play();
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}
}
