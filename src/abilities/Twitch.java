package abilities;

import java.awt.Point;

import effects.Twitching;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Twitch extends Ability
{
	public double maxDistFromTargetedPoint = 100;

	public Twitch(int p)
	{
		super("Twitch", p);
		cost = Math.min(4 - level, 1);
		costType = CostType.MANA;
		cooldown = Math.min(2 - level * 0.4, 0.1);
		range = 500;
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public Person getTarget(Environment env, Point targetPoint)
	{
		Person target = null;
		double shortestDistPow2 = maxDistFromTargetedPoint * maxDistFromTargetedPoint;
		for (Person p : env.people)
		{
			double distPow2 = Methods.DistancePow2(p.Point(), targetPoint);
			if (distPow2 < shortestDistPow2)
			{
				shortestDistPow2 = distPow2;
				target = p;
			}
		}
		return target;
	}

	public void use(Environment env, Person user, Point target1)
	{
		Person target = getTarget(env, target1);
		if (target == null)
			return;
		if (user.mana >= cost && cooldownLeft <= 0)
		{
			user.mana -= cost;
			cooldownLeft = cooldown;
			// ranged weapon check TODO
			// melee weapon check TODO
			// maintaining power check
			if (target.maintaining)
			{
				// stop maintaining
				Ability ability = target.abilities.get(target.abilityMaintaining);
				ability.use(env, target, user.target);
				target.abilityMaintaining = -1;
				target.maintaining = false;
				// punch
				for (int i = 0; i < target.abilities.size(); i++)
				{
					Ability punch = target.abilities.get(i);
					if (punch instanceof Punch)
						target.abilityTryingToRepetitivelyUse = i;
				}
			}
			else // if none of the above, stumble and fall
			{
				target.slip(true);
			}
			target.affect(new Twitching(this), true);
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.target = target;
		player.aimType = Player.AimType.TARGET_IN_RANGE;
	}
}
