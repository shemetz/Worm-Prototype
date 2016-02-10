package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class _LoopAbility extends Ability
{
	public int amount; // amount of time in seconds the target of the ability goes backwards in time.
	public boolean state;
	public boolean position;
	public double maxDistFromTargetedPoint = 500;

	enum Targeting
	{
		SELF, TARGETED, AREA
	};

	Targeting targeting;

	public _LoopAbility(String name, int p, String targeting1)
	{
		super(name, p);
		targeting = Targeting.valueOf(targeting1);
	}

	public List<Person> getTargets(Environment env, Person user, Point target)
	{
		List<Person> targets = new ArrayList<Person>();
		switch (targeting)
		{
		case SELF:
			targets.add(user);
			break;
		case TARGETED:
			double shortestDistPow2 = maxDistFromTargetedPoint * maxDistFromTargetedPoint;
			for (Person p : env.people)
			{
				double distPow2 = Methods.DistancePow2(p.Point(), target);
				if (distPow2 < shortestDistPow2)
				{
					shortestDistPow2 = distPow2;
					targets.clear();
					targets.add(p);
				}
			}
			break;
		case AREA:
			for (Person p : env.people)
			{
				double distPow2 = Methods.DistancePow2(p.Point(), target);
				if (distPow2 < maxDistFromTargetedPoint * maxDistFromTargetedPoint)
				{
					targets.add(p);
				}
			}
			break;
		default:
			MAIN.errorMessage("I'd get a chicken or something, I don't give a shit about chickens");
			break;
		}
		return targets;
	}

	public void use(Environment env, Person user, Point target)
	{
		List<Person> targets = getTargets(env, user, target);
		if (targets.isEmpty())
			return;
		if (user.mana >= cost && cooldownLeft <= 0)
		{
			user.mana -= cost;
			cooldownLeft = cooldown;

			for (Person p : targets)
				p.loop(amount, state, position);
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		target = player.Point();
		player.targetType = "loop";
	}
}
