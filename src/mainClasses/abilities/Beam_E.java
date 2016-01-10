package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Beam;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.Point3D;
import mainClasses.Vine;

public class Beam_E extends Ability
{

	int beamFrameNum;
	public Beam_E(String elementName, int p)
	{
		super("Beam <"+elementName+">", p);
		frameNum = 0;
		beamFrameNum = 0;
	}
	
	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x);

		/*
		 * Continuously shoot a beam at a target direction
		 * 
		 * OR
		 * 
		 * Lash a plant vine attack at a direction!
		 */
		if (!getElement().equals("Plant")) // not plant (which uses Vines)
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
				stopAllSounds();
			} else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
			{
				user.notAnimating = true;
				user.maintaining = true;
				on = true;
				user.switchAnimation(2);
				sounds.get(0).loop();
			}
		} else
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
				stopAllSounds();
			} else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
			{
				user.notAnimating = true;
				user.maintaining = true;
				on = true;
				// create vine
				final double vineExitDistance = 40;
				playSound("Beam");
				angle = angle + user.inaccuracyAngle; // rotation changes in updateTargeting
				Point3D start = new Point3D((int) (user.x + vineExitDistance * Math.cos(angle)), (int) (user.y + vineExitDistance * Math.sin(angle)), (int) user.z); // starts beamExitDistance pixels in front of the user
				Point3D end = new Point3D((int) (user.x + 2 * vineExitDistance * Math.cos(angle)), (int) (user.y + 2 * vineExitDistance * Math.sin(angle)), (int) user.z);
				Vine v = new Vine(user, start, end, points, range);
				env.vines.add(v);
				user.switchAnimation(2);
				sounds.get(0).loop();
			}
		}
	}
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		final double beamExitDistance = 40;
		if (!getElement().equals("Plant")) // non-plant
		{
			if (cooldownLeft == 0)
				if (user.mana >= costPerSecond * deltaTime)
				{
					playSound("Beam");
					angle = user.rotation + user.inaccuracyAngle; // rotation changes in updateTargeting
					Point3D start = new Point3D((int) (user.x + beamExitDistance * Math.cos(angle)), (int) (user.y + beamExitDistance * Math.sin(angle)), (int) user.z); // starts beamExitDistance pixels in front of the user
					// TODO piercing beams, or electric lightning bolts
					Point3D end = new Point3D((int) (user.x + range * Math.cos(angle)), (int) (user.y + range * Math.sin(angle)), (int) user.z);
					Beam b = new Beam(user, start, end, getElementNum(), points, range);
					frameNum++;
					b.frameNum = beamFrameNum;
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
				} else
				{
					stopSound("Beam");
					cooldownLeft = cooldown;
				}
		} else // plant vines
		{
			if (cooldownLeft == 0)
				if (user.mana >= costPerSecond * deltaTime)
				{
					if (!user.holdingVine) // create new vine after stopping mid-way
					{
						// create vine
						final double vineExitDistance = 40;
						playSound("Beam");
						angle = angle + user.inaccuracyAngle; // rotation changes in updateTargeting
						Point3D start = new Point3D((int) (user.x + vineExitDistance * Math.cos(angle)), (int) (user.y + vineExitDistance * Math.sin(angle)), (int) user.z); // starts beamExitDistance pixels in front of the user
						Point3D end = new Point3D((int) (user.x + 2 * vineExitDistance * Math.cos(angle)), (int) (user.y + 2 * vineExitDistance * Math.sin(angle)), (int) user.z);
						Vine v = new Vine(user, start, end, points, range);
						env.vines.add(v);
						env.moveVine(v, deltaTime);
					}
					user.mana -= costPerSecond * deltaTime;
				} else
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

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y-player.y, target.x-player.x);
		player.targetType = "look";
		if (!player.leftMousePressed && !player.holdingVine)
			player.rotate(angle, 3.0 * deltaTime);
		// if it's a vine and it's holding onto something, the person's rotation is hard-changed to the vine's angle in frame()
	}
}
