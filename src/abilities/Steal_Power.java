package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.Resources;
import mainClasses.VisualEffect;

public class Steal_Power extends Ability
{
	Ability stolenPower;
	Person stolee;
	int originalStolenPowerLevel;

	public Steal_Power(int p)
	{
		super("Steal Power", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		instant = false;

		stolenPower = null;
		stolee = null;
		originalStolenPowerLevel = -1;
	}

	public void updateStats()
	{
		cost = 3;
		costPerSecond = 0.5;
		cooldown = 1;
		range = 300 + level * 100;
		maxDistFromTargetedPoint = range;
	}

	public void use(Environment env, Person user, Point target1)
	{
		if (cooldownLeft <= 0)
			if (!on && user.mana >= cost)
			{
				Person target = getTarget(env, user, target1);
				if (target == null)
					return;

				Ability bestAbility = null;
				// Finds power with highest level
				for (Ability a : target.abilities)
					if (!a.natural)
						if (bestAbility == null || a.level > bestAbility.level)
							bestAbility = a;
				if (bestAbility != null)
				{
					stolee = target;
					stolenPower = bestAbility;
					stolenPower.disable(env, stolee);
					stolee.abilities.remove(stolenPower);

					originalStolenPowerLevel = stolenPower.level;
					stolenPower.level += 1;
					if (stolenPower.level > 10) // 10 is maximum
						stolenPower.level = 10;

					stolenPower.disabled = false;
					user.abilities.add(stolenPower);

					user.mana -= cost;
					cooldownLeft = cooldown;
					on = true;
				}

			}
			else if (on)
			{
				stop(env, user);
				cooldownLeft = cooldown;
				on = false;
			}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (user.mana >= costPerSecond * deltaTime)
		{
			// if stolee's out of range
			if (Methods.DistancePow2(stolee.Point(), user.Point()) > range * range)
				stop(env, user);
			else
			{
				user.mana -= costPerSecond * deltaTime;
				frameNum += 3; // 3 makes for a good-looking speed of beam effect

				VisualEffect visual = new VisualEffect();
				visual.timeLeft = deltaTime * 2;
				visual.frame = (999999 + frameNum - (int) Math.sqrt(Methods.DistancePow2(stolee.Point(), user.Point()))) % 100; // really high number because frameNum-distance mustn't be negative
				visual.p1 = new Point((int) user.x, (int) user.y);
				visual.p2 = new Point((int) stolee.x, (int) stolee.y);
				visual.type = VisualEffect.Type.CONNECTING_BEAM;
				visual.angle = Math.atan2(stolee.y - user.y, stolee.x - user.x);
				visual.image = Resources.abilities.get("steal");
				env.visualEffects.add(visual);
			}
		}
		else
		{
			stop(env, user);
		}
	}

	public void stop(Environment env, Person user)
	{
		stolenPower.disable(env, user);
		user.abilities.remove(stolenPower);
		stolenPower.level = originalStolenPowerLevel;
		stolenPower.disabled = false;
		stolee.abilities.add(stolenPower);
		stolee = null;
		stolenPower = null;
		on = false;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			stop(env, user);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.TARGET_IN_RANGE;
	}
}
