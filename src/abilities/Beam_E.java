package abilities;

import java.awt.Point;
import java.util.ArrayList;

import mainClasses.Ability;
import mainClasses.Beam;
import mainClasses.Environment;
import mainClasses.Evasion;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.Point3D;
import mainClasses.Vine;
import mainResourcesPackage.SoundEffect;

public class Beam_E extends _BeamAbility
{
	
	public Beam_E(String elementName, int p)
	{
		super("Beam <" + elementName + ">", p);
		costType = CostType.MANA;
		rangeType = RangeType.EXACT_RANGE;

		stopsMovement = false;
		maintainable = true;
		instant = true;

		frameNum = 0;
		beamFrameNum = 0;
		evasions = new ArrayList<Evasion>();

		if (elementName.equals("Plant"))
		{
			sounds.add(new SoundEffect("Vine_send.wav"));
			sounds.add(new SoundEffect("Vine_retrieve.wav"));
		}
		else
		{
			sounds.add(new SoundEffect(elementName + " Beam.wav"));
			sounds.get(0).endUnlessMaintained = true;
		}
	}

	public void updateStats()
	{
		if (getElement().equals("Plant"))
			range = 80 * level;
		else
			range = 500 * level;
		costPerSecond = 1;
		cooldown = 0.5; // after stopping a beam attack, this is the cooldown to start a new one
		damage = level * 0.25 * Ability.elementalAttackNumbers[elementNum][2] * Ability.elementalAttackNumbers[elementNum][0];
		pushback = level * 0.25 * Ability.elementalAttackNumbers[elementNum][2] * Ability.elementalAttackNumbers[elementNum][1];
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		double angle = 0;
		if (target != null)
			angle = Math.atan2(target.y - user.y, target.x - user.x);
		else
			on = true;// should always be true if target is null, but just in case.

		/*
		 * Continuously shoot a beam at a target direction
		 * 
		 * OR
		 * 
		 * Lash a plant vine attack at a direction!
		 */
		if (!getElement().equals("Plant")) // regular beam and not plant
		{
			if (on)
			{
				user.notAnimating = false;
				on = false;
				user.maintaining = false;
				if (user.mana <= 0.5)
					cooldownLeft = cooldown;
				for (int i = 0; i < env.beams.size(); i++)
					if (env.beams.get(i).creator.id == user.id)
					{
						env.beams.remove(i);
						i--;
					}
				criticalTimeLeft = 0;
			}
			else if (!user.prone && !user.maintaining && cooldownLeft <= 0 && user.timeEffect != 0)
			{
				user.notAnimating = true;
				user.maintaining = true;
				on = true;
				user.switchAnimation(2);
				evasions = new ArrayList<Evasion>();
				sounds.get(0).loop();
			}
		}
		else
		{
			// plant vines
			if (on)
			{
				user.notAnimating = false;
				user.maintaining = false;
				on = false;
				cooldownLeft = cooldown;
				for (int i = 0; i < env.vines.size(); i++) // TODO change this from removing vines to leaving the leftovers which just keep flying for about a second
					if (env.vines.get(i).creator.id == user.id)
					{
						env.vines.get(i).die();
						env.vines.remove(i);
						i--;
					}
			}
			else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
			{
				user.notAnimating = true;
				user.maintaining = true;
				on = true;
				// create vine
				final double vineExitDistance = 40;
				angle = angle + user.inaccuracyAngle; // rotation changes in updateTargeting
				Point3D start = new Point3D((int) (user.x + vineExitDistance * Math.cos(angle)), (int) (user.y + vineExitDistance * Math.sin(angle)), (int) user.z); // starts beamExitDistance pixels in front of the user
				Point3D end = new Point3D((int) (user.x + 2 * vineExitDistance * Math.cos(angle)), (int) (user.y + 2 * vineExitDistance * Math.sin(angle)), (int) user.z);
				Vine v = new Vine(user, start, end, damage, range);
				env.vines.add(v);
				user.switchAnimation(2);
				sounds.get(0).play();
			}
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		setSounds(user.Point());

		double targetAngle = Math.atan2(target.y - user.y, target.x - user.x);
		if (!user.holdingVine && !(user instanceof Player && ((Player) user).leftMousePressed))
			user.rotate(targetAngle, deltaTime * user.timeEffect);
		for (int j = 0; j < this.evasions.size(); j++)
			if (this.evasions.get(j).timeLeft > 0)
				this.evasions.get(j).timeLeft -= deltaTime;
			else
			{
				this.evasions.remove(j);
				j--;
			}
		if (criticalTimeLeft > 0)
			criticalTimeLeft -= deltaTime;

		final double beamExitDistance = 40;
		if (!getElement().equals("Plant")) // non-plant. Normal Beam.
		{
			if (cooldownLeft == 0)
				if (user.mana >= costPerSecond * deltaTime)
				{
					if (Math.random() * 0.24 < user.criticalChance) // I hope this 0.24 makes sense
						criticalTimeLeft = 1;
					sounds.get(0).loop();
					double angle = user.rotation + user.inaccuracyAngle;
					Point3D start = new Point3D((int) (user.x + beamExitDistance * Math.cos(angle)), (int) (user.y + beamExitDistance * Math.sin(angle)), user.z + user.height / 2); // starts beamExitDistance pixels in front of the user
					// TODO piercing beams, or electric lightning bolts
					Point3D end = new Point3D((int) (user.x + range * Math.cos(angle)), (int) (user.y + range * Math.sin(angle)), user.z + user.height / 2);
					Beam b = new Beam(user, this, start, end, elementNum, damage, pushback, range);
					frameNum++;
					b.frameNum = beamFrameNum;
					// critical chance
					if (criticalTimeLeft > 0)
						b.critical = true;
					env.beams.add(b);
					env.moveBeam(b, true, deltaTime);
					user.mana -= costPerSecond * deltaTime;
					frameNum++;
					if (frameNum >= 20)
					{
						beamFrameNum++;
						if (beamFrameNum >= 4)
							beamFrameNum = 0;
						frameNum = 0;
					}
				}
				else
				{
					cooldownLeft = cooldown;
				}
		}
		else // plant vines
		{
			if (cooldownLeft == 0)
				if (user.mana >= costPerSecond * deltaTime)
				{
					if (!user.holdingVine) // create new vine after stopping mid-way
					{
						// create vine
						final double vineExitDistance = 40;
						sounds.get(0).play();
						double angle = targetAngle + user.inaccuracyAngle; // rotation changes in updateTargeting
						Point3D start = new Point3D((int) (user.x + vineExitDistance * Math.cos(angle)), (int) (user.y + vineExitDistance * Math.sin(angle)), (int) user.z); // starts beamExitDistance pixels in front of the user
						Point3D end = new Point3D((int) (user.x + 2 * vineExitDistance * Math.cos(angle)), (int) (user.y + 2 * vineExitDistance * Math.sin(angle)), (int) user.z);
						Vine v = new Vine(user, start, end, damage, range);
						env.vines.add(v);
						env.moveVine(v, deltaTime);
					}
					user.mana -= costPerSecond * deltaTime;
				}
				else
				{
					cooldownLeft = cooldown;
					for (int i = 0; i < env.vines.size(); i++) // TODO change this from removing vines to leaving the leftovers which just keep flying for about a second
						if (env.vines.get(i).creator.id == user.id)
						{
							env.vines.get(i).retract();
						}
				}

		}
	}

	public void evadedBy(Person p)
	{
		evasions.add(new Evasion(p.id));
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, user.target);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
	}
}
