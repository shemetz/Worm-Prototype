package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;

public class Heal_I extends Ability
{

	public Heal_I(int p)
	{
		super("Heal I", p);
		cost = 0;
		costPerSecond = 1;
		costType = "mana";
		cooldown = 0;
		range = 50 * points;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = true;
		instant = true;
	}

	public void use(Environment env, Person user, Point target)
	{
		/*
		 * Continuously heal the closest injured person within range, or yourself.
		 */
		if (on)
		{
			on = false;
			user.maintaining = false;
			user.abilityMaintaining = -1;
		} else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
		{
			user.maintaining = true;
			on = true;
			targetEffect1 = 0;
		}
	}
	public void maintain (Environment env, Person user, Point target, double deltaTime)
	{

		if (costPerSecond * deltaTime <= user.mana)
		{
			if (frameNum % 6 == 0)
				frameNum++;
			if (frameNum >= 4)
				frameNum = 0;

			double shortestDistancePow2 = range * range;
			Person healingTarget = user;
			for (Person p : env.people)
				if (p != user)
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < shortestDistancePow2)
					{
						shortestDistancePow2 = distancePow2;
						if (targetEffect1 * targetEffect1 >= distancePow2)
							targetEffect1 -= 1 * points * deltaTime * (range + 20 - Math.sqrt(range - targetEffect1) - targetEffect1);
						healingTarget = p;
					}
				}
			healingTarget.affect(new Effect(deltaTime, "Healed", points), true);
			if (healingTarget != user)
			{
				VisualEffect healingEffect = new VisualEffect();
				healingEffect.timeLeft = deltaTime * 2;
				healingEffect.frame = frameNum;
				healingEffect.p1 = new Point((int) user.x, (int) user.y);
				healingEffect.p2 = new Point((int) healingTarget.x, (int) healingTarget.y);
				healingEffect.type = 3;
				healingEffect.angle = Math.atan2(healingTarget.y - user.y, healingTarget.x - user.x);

				env.effects.add(healingEffect);
			}
			user.mana -= deltaTime * costPerSecond;
		}
	}
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "";
		player.target = new Point(-1, -1);
	}
}
