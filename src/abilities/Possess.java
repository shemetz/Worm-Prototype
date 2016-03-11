package abilities;

import java.awt.Point;

import effects.Possessed;
import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Possess extends Ability
{
	Person original;
	Person victim;
	double maxDistFromTargetedPoint = 100;

	public Possess(int p)
	{
		super("Possess", p);
		cost = 7;
		costType = CostType.MANA;
		cooldown = 18 - level;
		range = 500;
		rangeType = RangeType.CIRCLE_AREA;

		duration = 3 * level; // or when reduced to < 20% HP
	}

	public Person getTarget(Environment env, Point targetPoint)
	{
		Person target = null;
		double shortestDistPow2 = maxDistFromTargetedPoint * maxDistFromTargetedPoint;
		for (Person p : env.people)
			if (!p.possessionVessel)
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

	public void use(Environment env, Person user, Point target)
	{
		if (user.possessingControllerID == -1) // no chain-possessions!
			if (!on && user.mana >= cost && cooldownLeft == 0 && !user.prone && !user.maintaining)
			{
				victim = getTarget(env, target);
				if (victim == null || victim.equals(user))
					return;

				on = true;
				original = user;
				if (user instanceof Player)
				{
					original.startStopPossession = true;
					original.possessionTargetID = victim.id;
					victim.possessionVessel = true;
					victim.possessedTimeLeft = duration;
					victim.possessingControllerID = user.id;
					victim.effects.add(new Possessed(duration, this));
					original.strengthOfAttemptedMovement = 0;
				}
				else
					MAIN.errorMessage("Don't forget to program the part in Possess for NPC-on-NPC possessions!");
			}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		// not sure if this code is even needed
		if (!victim.possessionVessel)
			victim.possessedTimeLeft -= deltaTime;
		if (victim.possessedTimeLeft <= 0)
			deactivate();
	}

	public void deactivate()
	{
		on = false;
		cooldownLeft = cooldown;
		// probably a mistake, I'm going to regret this
		for (Effect e : victim.effects)
			if (e instanceof Possessed)
				e.timeLeft = 0;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			deactivate();
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.TARGET_IN_RANGE;
		player.target = target;
	}
}
