package abilities;

import java.awt.Point;

import effects.Time_Stopped;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Time_Freeze_Target_I extends Ability
{
	public double maxDistFromTargetedPoint = 100; // NOTE: USED IN DRAWAIM!

	public Time_Freeze_Target_I(int p)
	{
		super("Time Freeze Target I", p);
		cost = 5;
		costType = CostType.MANA;
		cooldown = 3 * level;
		range = 500;
		rangeType = RangeType.CIRCLE_AREA;
		duration = 2 * level;
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
			Time_Stopped effect = new Time_Stopped(duration, this);
			target.affect(effect, true);
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.target = target;
		player.aimType = Player.AimType.TARGET_IN_RANGE;
	}
}
