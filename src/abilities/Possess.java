package abilities;

import java.awt.Point;

import effects.Possessed;
import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Person;
import mainClasses.Player;

public class Possess extends Ability
{
	Person original;
	Person victim;

	public Possess(int p)
	{
		super("Possess", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		cost = 7;
		cooldown = 18 - LEVEL;
		range = 500;
		duration = 3 * LEVEL; // or when reduced to < 20% HP

	}

	public boolean viableTarget(Person p, Person user)
	{
		if (p.equals(user))
			return false;
		if (p.possessionVessel)
			return false;
		if (p.dead)
			return false;
		return true;
	}

	public void use(Environment env, Person user, Point target)
	{
		if (user.possessingControllerID == -1) // no chain-possessions!
			if (!on && user.mana >= cost && cooldownLeft == 0 && !user.prone && !user.maintaining)
			{
				victim = getTarget(env, user, target);
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
