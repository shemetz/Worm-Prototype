package abilities;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;

/**
 * An Ability that applies some kind of effect on either the user itself, a targeted person, or a group of people in an area.
 * 
 * @author Itamar
 *
 */
public class _ApplyEffect extends Ability
{
	double beamAnimationTimer;

	/**
	 * SELF - only works on self.
	 * 
	 * OTHER - works on the closest person in range that isn't yourself. If there is none, works on self instead.
	 * 
	 * AREA - works on every person in range, including yourself.
	 * 
	 * TARGETED - works on the person closest to target point, if they're within range.
	 *
	 */
	public enum TargetType
	{
		SELF, OTHER, AREA, TARGETED
	};

	/**
	 * People under the effects of the ability.
	 */
	List<Person> targets;
	TargetType targetingType;
	VisualEffect.Type type;
	BufferedImage beamImage;

	/**
	 * Constructor.
	 * 
	 * @param name
	 *            the name of this ability
	 * @param level
	 *            the level of this ability
	 * @param targetType1
	 *            the TargetType (SELF, OTHER, AREA, TARGETED)
	 * @param type1
	 *            the VisualEffect Type
	 */
	public _ApplyEffect(String name, int level, TargetType targetType1, VisualEffect.Type type1)
	{
		super(name, level);
		targetingType = targetType1;
		type = type1;
		beamAnimationTimer = 0;
		targets = new ArrayList<Person>();
	}

	/**
	 * Adds targets to the {@link #targets} list, according to {@link #targetingType}.
	 * 
	 * @param env
	 *            the Environment
	 * @param user
	 *            the Ability user
	 */
	public void addNewTargets(Environment env, Person user)
	{
		switch (targetingType)
		{
		case SELF:
			targets.remove(user);
			targets.add(user);
			break;
		case OTHER:
			Person effectTarget = user;
			double shortestDistancePow2 = range * range;
			for (Person p : env.people)
				if (viableTarget(p, user))
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < shortestDistancePow2)
					{
						shortestDistancePow2 = distancePow2;
						effectTarget = p;
					}
				}
			if (targets.isEmpty())
				targets.add(effectTarget);
			// If changed target:
			else if (!effectTarget.equals(targets.get(0)))
			{
				targets.get(0).affect(effect(), false);
				targets.remove(0);
				targets.add(effectTarget);
			}
			// Else, targets were not changed
			break;
		case AREA:
			targets.add(user);
			for (Person p : env.people)
				if (viableTarget(p, user))
				{
					double distancePow2 = Methods.DistancePow2(user.x, user.y, p.x, p.y);
					if (distancePow2 < range * range)
						targets.add(p);
				}
			break;
		case TARGETED:
			targets.clear();
			double closest = 100 * 100; // max distance of 100 pixels to target, from cursor
			for (Person p : env.people)
			{
				if (viableTarget(p, user))
				{
					double distancePow2 = Methods.DistancePow2(user.target.x, user.target.y, p.x, p.y);
					if (distancePow2 < closest)
					{
						closest = distancePow2;
						targets.clear();
						targets.add(p);
					}
				}
			}
			break;
		default:
			MAIN.errorMessage(targetingType);
			break;
		}
	}

	/**
	 * Adds a {@link VisualEffect} to the environment, if effectTarget is not user. The effect will be created between the user and the target. It is usually a beam-like animation.
	 * 
	 * @param env
	 * @param deltaTime
	 * @param user
	 * @param effectTarget
	 */
	public void addVisualEffect(Environment env, double deltaTime, Person user, Person effectTarget)
	{
		if (effectTarget != user)
		{
			VisualEffect visual = new VisualEffect();
			visual.timeLeft = deltaTime * 2;
			visual.frame = 99 - ((frameNum + (int) Math.sqrt(Methods.DistancePow2(effectTarget.Point(), user.Point()))) % 100);
			visual.p1 = new Point((int) user.x, (int) user.y);
			visual.p2 = new Point((int) effectTarget.x, (int) effectTarget.y);
			visual.type = type;
			visual.angle = Math.atan2(effectTarget.y - user.y, effectTarget.x - user.x);
			visual.image = beamImage;

			env.visualEffects.add(visual);
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (maintainable && on)
		{
			use(env, user, user.target);
		}
	}

	public void use(Environment env, Person user, Point targetPoint)
	{
		if (maintainable)
		{
			if (on)
			{
				on = false;
				user.maintaining = false;
				user.abilityMaintaining = -1;
				for (Person target : targets)
				{
					target.affect(effect(), false);
					// uh add visual effect ? TODO
				}
				targets.clear();
			}
			else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
			{
				user.maintaining = true;
				on = true;
			}
		}
		else
		{
			addNewTargets(env, user);
			if (user.mana >= cost && !user.maintaining && cooldownLeft <= 0 && !user.prone)
			{
				if (!targets.isEmpty())
				{
					user.mana -= cost;
					cooldownLeft = cooldown;
					for (Person p : targets)
					{
						p.affect(effect(), true);
					}
				}
			}
		}
	}

	public void maintain(Environment env, Person user, Point targetPoint, double deltaTime)
	{
		beamAnimationTimer += deltaTime;
		frameNum += 3;
		if (costPerSecond * deltaTime <= user.mana)
		{
			addNewTargets(env, user);
			// Stop affecting anyone out of range
			for (int i = 0; i < targets.size(); i++)
			{
				if (Methods.DistancePow2(targets.get(i).Point(), user.Point()) > range * range)
				{
					targets.get(i).affect(effect(), false);
					targets.remove(i);
					i--;
				}
			}
			for (Person target : targets)
			{
				boolean alreadyAffected = false;
				for (Effect e : target.effects)
					if (e.creatorAbility != null && e.creatorAbility.equals(this))
						alreadyAffected = true;
				if (!alreadyAffected)
					target.affect(effect(), true);
				addVisualEffect(env, deltaTime, user, target);
			}
			user.mana -= deltaTime * costPerSecond;
		}
	}

	/**
	 * 
	 * @return the Effect that this method applies on the targets
	 */
	public Effect effect()
	{
		MAIN.errorMessage("This needs to be a special effect....this is not supposed to be an ApplyEffect");
		return null;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		if (targetingType == TargetType.TARGETED)
		{
			player.aimType = Player.AimType.TARGET_IN_RANGE;
		}
		else
		{
			player.aimType = Player.AimType.NONE;
			player.target = new Point(-1, -1);
		}
	}
}
