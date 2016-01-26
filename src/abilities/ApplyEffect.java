package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Main;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;

public class ApplyEffect extends Ability
{
	double beamAnimationTimer;

	public enum targetTypes
	{
		SELF, OTHER, AREA, TARGETED
	};

	// SELF - only works on self.
	// OTHER - works on the closest person in range that isn't yourself. IF there is none, works on self instead.
	// AREA - works on every person in range, including yourself.
	// TARGETED - works on the person closest to target point, if they're within range.
	List<Person>	targets;
	targetTypes		targetingType;
	int				visualType;

	public ApplyEffect(String name, int p, targetTypes targetType1, int visual)
	{
		super(name, p);
		targetingType = targetType1;
		visualType = visual;
		beamAnimationTimer = 0;
		targets = new ArrayList<Person>();
	}

	public void addNewTargets(Environment env, Person user)
	{
		List<Person> newTargets = new ArrayList<Person>();
		switch (targetingType)
		{
		case SELF:
			newTargets.add(user);
			break;
		case OTHER:
			Person effectTarget = user;
			double shortestDistancePow2 = range * range;
			for (Person p : env.people)
				if (p != user)
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < shortestDistancePow2)
					{
						shortestDistancePow2 = distancePow2;
						effectTarget = p;
					}
				}
			newTargets.add(effectTarget);
			break;
		default:
			Main.errorMessage(targetingType);
			break;
		}

		// Only add new targets, without having duplicates
		targets.removeAll(newTargets);
		targets.addAll(newTargets);
	}

	public void draw(Environment env, double deltaTime, Person user, Person effectTarget)
	{
		if (effectTarget != user)
		{
			VisualEffect visual = new VisualEffect();
			visual.timeLeft = deltaTime * 2;
			visual.frame = 99 - (frameNum % 100);
			visual.p1 = new Point((int) user.x, (int) user.y);
			visual.p2 = new Point((int) effectTarget.x, (int) effectTarget.y);
			visual.type = visualType;
			visual.angle = Math.atan2(effectTarget.y - user.y, effectTarget.x - user.x);

			env.visualEffects.add(visual);
		}
	}

	public void use(Environment env, Person user, Point targetPoint)
	{
		if (on)
		{
			on = false;
			user.maintaining = false;
			user.abilityMaintaining = -1;
			for (Person target : targets)
			{
					target.affect(effect(), false);
					//uh add visual effect ?  TODO
			}
			targets.clear();
		} else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
		{
			user.maintaining = true;
			on = true;
		}
	}

	public void maintain(Environment env, Person user, Point targetPoint, double deltaTime)
	{
		beamAnimationTimer += deltaTime;
		frameNum += 3;
		if (costPerSecond * deltaTime <= user.mana)
		{
			boolean userWasTarget = targets.contains(user);
			addNewTargets(env, user);
			boolean userIsNowTarget = targets.contains(user);
			if (userWasTarget && !userIsNowTarget)
				user.affect(effect(), false);
			for (int i = 0; i < targets.size(); i++)
			{
				if (Methods.DistancePow2(targets.get(i).Point(), user.Point()) > range * range)
				{
					System.out.println(targets.size());
					targets.get(i).affect(effect(), false);
					targets.remove(i);
					i--;
				}
			}
			for (Person target : targets)
			{
				boolean alreadyAffected = false;
				for (Effect e : target.effects)
					if (e.creatorAbility.equals(this))
						alreadyAffected = true;
				if (!alreadyAffected)
					target.affect(effect(), true);
				draw(env, deltaTime, user, target);
			}
			user.mana -= deltaTime * costPerSecond;
		}
	}

	public Effect effect()
	{
		Main.errorMessage("This needs to be a special effect....this is not supposed to be an ApplyEffect");
		return null;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		// Override this if the ability is using TARGETED
		player.targetType = "";
		player.target = new Point(-1, -1);
	}
}
