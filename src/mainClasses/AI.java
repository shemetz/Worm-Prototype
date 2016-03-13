package mainClasses;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import abilities.Ball_E;
import abilities.Beam_E;
import abilities.Heal_I;
import abilities.Heal_II;
import abilities.Punch;
import pathfinding.WayPoint;

public class AI
{
	NPC npc;
	Environment env;
	MAIN main;

	public AI(NPC npc1, Environment env1, MAIN main1)
	{
		npc = npc1;
		env = env1;
		main = main1;
	}

	public void HEAL()
	{
		for (Ability a : npc.abilities)
			if (a instanceof Heal_I || a instanceof Heal_II)
			{
				main.pressAbilityKey(a, true, npc);
				return;
			}
		MAIN.errorMessage("WRONG NO");
	}

	public void CIRCLE_STRAFE(Person targetPerson, double deltaTime)
	{
		double angleToTarget = Math.atan2(targetPerson.y - npc.y, targetPerson.x - npc.x);
		double distanceToTargetPow2 = Methods.DistancePow2(npc.x, npc.y, targetPerson.x, targetPerson.y);
		// move around target. Also, get close to it or away from it to get into the "circle strafing" range.
		npc.rotate(angleToTarget, deltaTime);

		// Fucking fuck shit code is not fucking working and I have no bloody idea what's wrong with it and I rewrote it to something that looks different but does the same and guess what? yes the bug is still happening. gahhhhhhhhhhhhh,
		// hopefully one day I will figure out what's wrong here.

		// double diffAngleFromTarget = Math.asin(7097.42205 * globalDeltaTime * 0.5 / Math.sqrt(distanceToTargetPow2)); // KILL ME
		// double newAngle = -Math.PI + angleToTarget + diffAngleFromTarget * (p.rightOrLeft ? 1 : -1);
		// double newX = target.x + dist * Math.cos(newAngle);
		// double newY = target.y + dist * Math.sin(newAngle);
		// p.directionOfAttemptedMovement = Math.atan2(newY - p.y, newX - p.x);
		// p.strengthOfAttemptedMovement = 1;
		// if (random.nextDouble() < 0.01) // chance of switching direction mid-circle
		// p.rightOrLeft = !p.rightOrLeft;
		double deviationAngle = Math.acos(3062.332465 * deltaTime * 0.5 / Math.sqrt(distanceToTargetPow2)); // Haha I'm just throwing random numbers that make it stop growing
		if (Double.isNaN(deviationAngle))
		{
			// ughhhhhhhhh
			deviationAngle = Math.PI / 1;
		}

		npc.directionOfAttemptedMovement = angleToTarget + deviationAngle * (npc.rightOrLeft ? 1 : -1); // BUG - distance grows between p and target for some reason!!!! TODO
		npc.strengthOfAttemptedMovement = 1;

		if (npc.timeSinceLastHit == 0 || npc.justCollided || Math.random() < 0.005) // chance of switching direction mid-circle
			npc.rightOrLeft = !npc.rightOrLeft;

		// moving away or into range
		// range is ALWAYS between 250 and 500 cm, because....because I said so
		// TODO ....yeah...
		if (distanceToTargetPow2 < 250 * 250)
			npc.directionOfAttemptedMovement = Methods.meanAngle(angleToTarget + Math.PI, npc.directionOfAttemptedMovement);
		if (distanceToTargetPow2 > 500 * 500)
			npc.directionOfAttemptedMovement = Methods.meanAngle(angleToTarget, npc.directionOfAttemptedMovement);

		// Attacking
		for (int aIndex = 0; aIndex < npc.abilities.size(); aIndex++)
		{
			Ability a = npc.abilities.get(aIndex);
			if (a.hasTag("projectile")) // ball
				if (a instanceof Ball_E)
				{
					// aim the ball the right direction, taking into account the velocity addition caused by the person moving
					double v = Ball.giveVelocity();
					double xv = v * Math.cos(angleToTarget);
					double yv = v * Math.sin(angleToTarget);
					xv -= targetPerson.xVel;
					yv -= targetPerson.yVel;
					npc.target = new Point((int) (targetPerson.x + xv), (int) (targetPerson.y + yv));
					main.pressAbilityKey(aIndex, true, npc);
				}
			if (a instanceof Beam_E) // beam
			{
				// aims the beam exactly at the target, so will miss often
				npc.target = new Point((int) (targetPerson.x), (int) (targetPerson.y));
				main.pressAbilityKey(aIndex, true, npc);
			}
		}
	}

	public void CHASE(Person targetPerson, double deltaTime)
	{
		double angleToTarget = Math.atan2(targetPerson.y - npc.y, targetPerson.x - npc.x);
		double distanceToTargetPow2 = Methods.DistancePow2(npc.x, npc.y, targetPerson.x, targetPerson.y);

		npc.timeSinceLastDistCheck += deltaTime;
		// move towards target via pathfinding:
		boolean blocked = npc.envMap.blocked(npc, (int) (npc.x / 96), (int) (npc.y / 96));
		if (distanceToTargetPow2 > Math.pow(96, 2)) // If distance to target > 2 blocks
		{
			if (npc.timeSinceLastDistCheck >= 1) // once per second. that variable is reduced by 1 soon after npc
			{
				Point targetPoint = new Point((int) (targetPerson.x + targetPerson.xVel * deltaTime * 4), (int) (targetPerson.y + targetPerson.yVel * deltaTime * 4));
				npc.path = npc.pathFind(targetPoint);
			}

			// move according to pathfinding
			npc.updatePath(env);
			if (npc.path != null && !npc.path.isEmpty())
			{
				angleToTarget = Math.atan2(npc.path.get(0).y - npc.y, npc.path.get(0).x - npc.x);
				npc.target = new Point(npc.path.get(0).x, npc.path.get(0).y);
			}

			// even if path is finished, will just walk towards targeted person
			npc.rotate(angleToTarget, deltaTime);
			npc.directionOfAttemptedMovement = angleToTarget;
			npc.strengthOfAttemptedMovement = 1;
		}
		// if close to target but blocked and target's getting away
		else if (blocked && npc.lastDistPow2 > distanceToTargetPow2)
		{
			if (npc.timeSinceLastDistCheck >= 1) // once per second. that variable is reduced by 1 soon after npc
			{
				Point targetPoint = new Point((int) (npc.x) + (int) (Math.random() * 3) - 1, (int) (npc.y) + (int) (Math.random() * 3) - 1);
				npc.path = npc.pathFind(targetPoint);
			}

			// move according to pathfinding
			npc.updatePath(env);
			if (npc.path != null && !npc.path.isEmpty())
			{
				angleToTarget = Math.atan2(npc.path.get(0).y - npc.y, npc.path.get(0).x - npc.x);
				npc.target = new Point(npc.path.get(0).x, npc.path.get(0).y);
			}

			// even if path is finished, will just walk towards targeted person
			npc.rotate(angleToTarget, deltaTime);
			npc.directionOfAttemptedMovement = angleToTarget;
			npc.strengthOfAttemptedMovement = 1;
		}
		// if close to target and not blocked for long
		else
		{
			npc.rotate(angleToTarget, deltaTime);
			npc.directionOfAttemptedMovement = angleToTarget;
			npc.strengthOfAttemptedMovement = 1;
			npc.target = new Point((int) targetPerson.x, (int) targetPerson.y);
		}

		if (npc.timeSinceLastDistCheck >= 1) // Check distance every second
		{
			npc.lastDistPow2 = distanceToTargetPow2;
			npc.timeSinceLastDistCheck -= 1;
		}
	}

	public void RETREAT(Person targetPerson, double deltaTime)
	{
		double angleToTarget = -1;
		if (targetPerson != null)
		{
			double distanceToTargetPow2 = Methods.DistancePow2(npc.x, npc.y, targetPerson.x, targetPerson.y);
			angleToTarget = Math.atan2(targetPerson.y - npc.y, targetPerson.x - npc.x);
			angleToTarget += Math.PI; // because you know, away from danger
			npc.timeSinceLastDistCheck += deltaTime;
			// Back away from any enemy nearby when low on health
			if (npc.timeSinceLastDistCheck >= 1) // once per second.
			{
				Point targetPoint = null;
				List<WayPoint> path = null;
				int attempts = 0;
				double[] angleAttempts =
				{ 0.0, 0.3, -0.3, 0.6, -0.6 };
				// test multiple angles ahead
				while (path == null && attempts < angleAttempts.length)
				{
					double angle = angleToTarget + angleAttempts[attempts];
					// test multiple distances
					for (int i = 1000; i >= 300 && path == null; i -= 50) // 1000 to 300
					{
						targetPoint = new Point((int) (targetPerson.x + i * Math.cos(angle)), (int) (targetPerson.y + i * Math.sin(angle)));
						path = npc.pathFind(targetPoint);
					}
					attempts++;
				}
				if (path == null) // srsly?
					path = new ArrayList<WayPoint>(); // empty path
				npc.path = path;

				npc.lastDistPow2 = distanceToTargetPow2;
				npc.timeSinceLastDistCheck -= 1;
			}
			// move according to pathfinding
			npc.updatePath(env);
			if (npc.path != null && !npc.path.isEmpty())
			{
				angleToTarget = Math.atan2(npc.path.get(0).y - npc.y, npc.path.get(0).x - npc.x);
				npc.target = new Point(npc.path.get(0).x, npc.path.get(0).y);
			}

			// move to the point
			npc.rotate(angleToTarget, deltaTime);
			npc.directionOfAttemptedMovement = angleToTarget;
			npc.strengthOfAttemptedMovement = 1;
		}

		for (Ability a : npc.abilities)
			// use heal if possible
			if (a instanceof Heal_I || a instanceof Heal_II)
				HEAL();
	}

	public void PANIC(double deltaTime)
	{
		// run around aimlessly
		if (npc.frameNum % 40 == 0)
			npc.directionOfAttemptedMovement = npc.rotation - 0.5 * Math.PI + Math.random() * Math.PI; // random direction in 180 degree arc
		npc.rotate(npc.directionOfAttemptedMovement, deltaTime);
		npc.strengthOfAttemptedMovement = 1;
	}

	public void PUNCH(Person targetPerson)
	{
		double distanceToTargetPow2 = Methods.DistancePow2(npc.x, npc.y, targetPerson.x, targetPerson.y);
		Ability punch = null;
		int index = -1;
		for (int i = 0; i < npc.abilities.size(); i++)
			if (npc.abilities.get(i) instanceof Punch)
			{
				index = i;
				punch = npc.abilities.get(index);
			}
		double maxDistanceNeeded = punch.range + targetPerson.radius;
		for (ArcForceField aff : env.AFFs)
			if (aff.target.equals(targetPerson))
			{
				// angle check
				double angleToMe = Math.atan2(npc.y - targetPerson.y, npc.x - targetPerson.x);
				while (aff.rotation > Math.PI)
					aff.rotation -= 2 * Math.PI;
				while (aff.rotation < -Math.PI)
					aff.rotation += 2 * Math.PI;
				if (angleToMe > aff.rotation - aff.arc / 2 && angleToMe < aff.rotation + aff.arc / 2)
					maxDistanceNeeded = punch.range + aff.maxRadius;
			}
		// PUNCH if in range
		if (distanceToTargetPow2 < Math.pow(maxDistanceNeeded, 2))
			main.pressAbilityKey(index, true, npc);
		else if (punch.cooldownLeft <= 0)
		{
			main.pressAbilityKey(index, false, npc); // stop punching
		}
	}
}
