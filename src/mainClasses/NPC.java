package mainClasses;

import java.awt.Point;
import java.awt.geom.Point2D;

import abilities.Punch;

public class NPC extends Person
{
	String	strategy;				// "aggressive" = attack enemies if possible, then heal/buff and follow if possible.
									// "defensive" = push away enemies and block them if possible, run away if possible.
									// "passive" = does nothing.
									//
	String	tactic;					// "circle strafing" = move in a constant angular direction around the target, shooting at it when able. When hitting something or randomly while moving, change direction.
									// "move into position" = move towards or away from the target until in optimalRange, strafing left or right when being hit by something. If can't see target, stop.
	boolean	hasAllies;

	int		targetID		= -1;
	boolean	rightOrLeft;			// true = right or CW. false = left or CCW.
	boolean	justCollided	= false;
	boolean	justGotHit		= false;
	double	instinctDelayTime;
	double	timeSinceLastInstinct;
	double	angleOfLastInstinct;

	public NPC(int x1, int y1, String s1)
	{
		super(x1, y1);
		hasAllies = false;
		// TEMP
		strategy = s1;
		tactic = "no target";
		rightOrLeft = true;
		timeSinceLastInstinct = instinctDelayTime;
		angleOfLastInstinct = 0;
		rename(); // random npc name - no abilities yet
	}

	public void updateSubStats()
	{
		basicUpdateSubStats();
		instinctDelayTime = Math.max(0.15 - 0.015 * WITS, 0); // average is 0.35 secs
	}

	public void setCommander(int comID)
	{
		commanderID = comID;
		hasAllies = true;
	}

	public boolean viableTarget(Person enemy)
	{
		if (enemy.commanderID == commanderID)
			return false;
		if (enemy.z >= z + height) //TODO remove this when ground-to-air combat is more possible
			return false;
		if (enemy.dead)
			return false;
		return true;
	}

	void frameAIupdate(double deltaTime, int frameNum, Environment env, Main main)
	{
		// choose target
		switch (this.tactic)
		{
		case "retreat":
		case "circle strafing":
		case "punch chasing":
			if (frameNum % 25 == 0) // don't check a lot of times in a short period
			{
				// Choose as target the closest enemy. There's no range limit to this. TODO
				double shortestDistanceToTargetPow2 = Double.MAX_VALUE;
				for (Person p2 : env.people)
					if (this.viableTarget(p2))
						if (Methods.DistancePow2(this.x, this.y, p2.x, p2.y) < shortestDistanceToTargetPow2)
						{
							shortestDistanceToTargetPow2 = Methods.DistancePow2(this.x, this.y, p2.x, p2.y);
							this.targetID = p2.id;
						}
			}
			break;
		case "no target":
		case "panic":
		default:
			this.targetID = -1;
			break;
		}
		Person targetPerson = null;
		if (this.targetID != -1 && this.tactic != "no target")
			for (Person p2 : env.people)
				if (p2.id == this.targetID)
				{
					targetPerson = p2;
					break;
				}
		if (targetPerson != null)
		{
			double angleToTarget = Math.atan2(targetPerson.y - this.y, targetPerson.x - this.x);
			double distanceToTargetPow2 = Methods.DistancePow2(this.x, this.y, targetPerson.x, targetPerson.y);
			// target-type tactics
			switch (this.tactic)
			{
			case "punch chasing":
				// move towards target and punch them.
				this.rotate(angleToTarget, deltaTime);
				this.directionOfAttemptedMovement = angleToTarget;
				this.strengthOfAttemptedMovement = 1;
				this.target = new Point((int) targetPerson.x, (int) targetPerson.y);
				Ability punch = null;
				int index = -1;
				for (int i = 0; i < this.abilities.size(); i++)
					if (this.abilities.get(i) instanceof Punch)
					{
						index = i;
						punch = this.abilities.get(index);
					}
				if (punch != null)
					if (distanceToTargetPow2 < Math.pow(punch.range + targetPerson.radius / 2, 2))
						main.pressAbilityKey(index, true, this);
					else if (punch.cooldownLeft <= 0)
						main.pressAbilityKey(index, false, this);
				break;
			case "circle strafing":
				// move around target. Also, get close to it or away from it to get into the "circle strafing" range.
				this.rotate(angleToTarget, deltaTime);

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

				this.directionOfAttemptedMovement = angleToTarget + deviationAngle * (this.rightOrLeft ? 1 : -1); // BUG - distance grows between p and target for some reason!!!! TODO
				this.strengthOfAttemptedMovement = 1;

				if (this.justGotHit || this.justCollided || Math.random() < 0.005) // chance of switching direction mid-circle
					this.rightOrLeft = !this.rightOrLeft;

				// moving away or into range
				// range is ALWAYS between 250 and 500 cm, because....because I said so
				// TODO ....yeah...
				if (distanceToTargetPow2 < 250 * 250)
					this.directionOfAttemptedMovement = Methods.meanAngle(angleToTarget + Math.PI, this.directionOfAttemptedMovement);
				if (distanceToTargetPow2 > 500 * 500)
					this.directionOfAttemptedMovement = Methods.meanAngle(angleToTarget, this.directionOfAttemptedMovement);

				// Attacking
				for (int aIndex = 0; aIndex < this.abilities.size(); aIndex++)
				{
					Ability a = this.abilities.get(aIndex);
					if (a.hasTag("projectile")) // ball
						if (a.justName().equals("Ball"))
						{
							// aim the ball the right direction, taking into account the velocity addition caused by the person moving
							double v = Ball.giveVelocity(a.level);
							double xv = v * Math.cos(angleToTarget);
							double yv = v * Math.sin(angleToTarget);
							xv -= this.xVel;
							yv -= this.yVel;
							this.target = new Point((int) (this.x + xv), (int) (this.y + yv));
							main.pressAbilityKey(aIndex, true, this);
						}
					if (a.hasTag("beam")) // beam
						if (a.justName().equals("Beam"))
						{
							// aims the beam exactly at the target, so will miss often
							this.target = new Point((int) (this.x), (int) (this.y));
							main.pressAbilityKey(aIndex, true, this);
						}
				}
				break;
			case "retreat":
				// Back away from any enemy nearby when low on health
				this.strengthOfAttemptedMovement = 0; // stop if there's nobody to retreat from
				double shortestSquaredDistance = 400000; // minimum distance to keep from enemies. About 7 tiles
				if (distanceToTargetPow2 < shortestSquaredDistance)
				{
					this.directionOfAttemptedMovement = angleToTarget + Math.PI; // away from target
					this.rotate(this.directionOfAttemptedMovement, deltaTime);
					this.strengthOfAttemptedMovement = 1;
				}
				break;
			default:
				Main.errorMessage("6j93k, no target-tactic - " + this.tactic);
				break;
			}
		} else // no-target tactics
			switch (this.tactic)
			{
			case "panic":
				// run around aimlessly
				if (frameNum % 40 == 0)
					this.directionOfAttemptedMovement = this.rotation - 0.5 * Math.PI + Math.random() * Math.PI; // random direction in 180 degree arc
				this.rotate(this.directionOfAttemptedMovement, deltaTime);
				this.strengthOfAttemptedMovement = 1;
				break;
			case "circle strafing":
			case "punch chasing":
				// waiting for tactic-switching
				break;
			case "no target":
				// don't move
				this.strengthOfAttemptedMovement = 0;
				// try to switch tactics

				if (this.strategy.equals("aggressive"))
				{
					// Choose as target the closest enemy.
					double shortestDistanceToTargetPow2 = Double.MAX_VALUE;
					for (Person p2 : env.people)
						if (this.viableTarget(p2))
							if (Methods.DistancePow2(this.x, this.y, p2.x, p2.y) < shortestDistanceToTargetPow2)
							{
								shortestDistanceToTargetPow2 = Methods.DistancePow2(this.x, this.y, p2.x, p2.y);
								this.targetID = p2.id; // not necessary?
								targetPerson = p2;
							}
					if (targetPerson != null)
						if (Methods.DistancePow2(this.x, this.y, targetPerson.x, targetPerson.y) < 600 * 600) // 600 sounds like an OK number
							if (this.mana > 0.4 * this.maxMana)
								this.tactic = "circle strafing";
							else if (this.stamina > 0.1 * this.maxStamina)
								this.tactic = "punch chasing";
							else
								this.tactic = "retreat";
				}
				if (this.strategy.equals("passive")) // TEMP TODO
				{
					if (this.justGotHit)
						for (int aIndex = 0; aIndex < this.abilities.size(); aIndex++)
						{
							Ability a = this.abilities.get(aIndex);
							if (a.hasTag("defensive") && a.instant)
							{
								main.pressAbilityKey(aIndex, true, this); // activate defensive ability
								this.abilityTryingToRepetitivelyUse = -1;
							}
						}
				}
				break;

			default:
				Main.errorMessage("shpontzilontz. no no-target-tactic - " + this.tactic);
				break;
			}
		// tactic-switching decisions. TODO make it make sense
		String prevTactic = "" + this.tactic; // copy
		if (this.panic)
			this.tactic = "panic";
		else if (this.life < 0.15 * this.maxLife)
			this.tactic = "retreat";
		else if (this.tactic.equals("retreat")) // stop retreating when uninjured
			this.tactic = "no target";
		else if (this.strategy.equals("aggressive"))
		{
			if (this.tactic.equals("circle strafing") && this.mana <= 0.1 * this.maxMana)
				this.tactic = "punch chasing";
			else if (this.tactic.equals("punch chasing") && this.mana >= 0.9 * this.maxMana)
				this.tactic = "circle strafing";
		}
		if (!prevTactic.equals(this.tactic))
		{
			if (this.abilityMaintaining != -1)
				main.pressAbilityKey(this.abilityMaintaining, false, this);
			this.abilityMaintaining = -1;
			this.abilityAiming = -1;
			this.abilityTryingToRepetitivelyUse = -1;
		}
		// resetting check-booleans
		this.justCollided = false;
		this.justGotHit = false;

		if (Double.isNaN(this.directionOfAttemptedMovement)) // to fix a certain irritating bug
		{
			this.directionOfAttemptedMovement = 0;
			this.strengthOfAttemptedMovement = 0;
		}
		boolean nothingToDo = false;
		if (this.strengthOfAttemptedMovement == 0)
			nothingToDo = true;

		// Instincts - move away from dangerous objects
		if (this.timeSinceLastInstinct < 0)
			this.timeSinceLastInstinct += deltaTime;
		if (moveAwayFromDangerousObjects(env, !nothingToDo))
			if (nothingToDo)
				this.rotate(this.directionOfAttemptedMovement, deltaTime);
			else if (this.timeSinceLastInstinct >= 0 && this.timeSinceLastInstinct < this.instinctDelayTime)
			{
				// for a period of instinctDelayTime after finishing with instincts, keep moving
				if (nothingToDo)
					this.directionOfAttemptedMovement = this.angleOfLastInstinct;
				this.strengthOfAttemptedMovement = 1;
				this.timeSinceLastInstinct += deltaTime;
			}
	}

	boolean moveAwayFromDangerousObjects(Environment env, boolean considerAttempt)
	{
		/*
		 * INSTINCTS
		 * 
		 * Every NPC has an instinct delay (instinctDelayTime, IDT), and a timer (timeSinceLastInstinct, TSLI).
		 * 
		 * When there's a danger, TSLI will start counting for IDT seconds by becoming -IDT and growing by deltaTime per frame.
		 * 
		 * When TSLI reaches 0, it stops and the person starts instinct-ing as normal.
		 * 
		 * When a person is no longer under instincts - no danger nearby - TSLI will begin counting again, upwards
		 * 
		 * While it's counting again, the person is under the instinct delay influence - moving in the direction of last instinct.
		 * 
		 * After it reaches the value of IDT, it stops and the person returns to normal, until new danger is detected.
		 * 
		 * Functionally, a person's reactions to danger are delayed by IDT (but not while reacting - not that it matters much).
		 */
		final double maximumDistanceICareAboutPow2 = Math.pow(WITS * 70, 2); // TODO

		double xElement = 0;
		double yElement = 0;
		for (Ball b : env.balls)
		{
			double distancePow2 = Methods.DistancePow2(b.Point(), this.Point());
			if (distancePow2 < maximumDistanceICareAboutPow2)
			{
				Point somewhereReallyFarAway = new Point((int) (b.x + 1000 * b.xVel), (int) (b.y + 1000 * b.yVel));
				Point2D closestPointOnLine = Methods.getClosestPointOnLine(b.x, b.y, somewhereReallyFarAway.x, somewhereReallyFarAway.y, this.x, this.y);
				double distanceToLinePow2 = Methods.DistancePow2(this.x, this.y, closestPointOnLine.getX(), closestPointOnLine.getY());
				if (distanceToLinePow2 < Math.pow(this.radius + b.radius + 10, 2)) // if it can hit
				{
					if ((b.x > closestPointOnLine.getX()) != (b.xVel > 0)) // could also do the same with yVel
					{
						boolean clockwise = (b.x - this.x) * Math.sin(b.angle()) > (b.y - this.y) * Math.cos(b.angle());
						double peakAngleToPerson = b.angle() + (clockwise ? Math.PI / 2 : -Math.PI / 2);
						xElement += Math.cos(peakAngleToPerson) * b.getDamage() / distancePow2;
						yElement += Math.sin(peakAngleToPerson) * b.getDamage() / distancePow2;
					}
				}
			}
		}
		for (Beam b : env.beams)
		{
			Point2D closestPointOnLine = Methods.getClosestPointOnLine(b.start.x, b.start.y, b.end.x, b.end.y, this.x, this.y);
			if (closestPointOnLine == null)
				continue; // no point in trying to escape null beams
			double distanceToLinePow2 = Methods.DistancePow2(this.x, this.y, closestPointOnLine.getX(), closestPointOnLine.getY());
			if (distanceToLinePow2 < maximumDistanceICareAboutPow2)
			{
				if ((b.start.x > closestPointOnLine.getX()) != (b.end.x - b.start.x > 0))
				{
					boolean clockwise = (b.start.x - this.x) * Math.sin(b.angle()) > (b.start.y - this.y) * Math.cos(b.angle());
					double peakAngleToPerson = b.angle() + (clockwise ? Math.PI / 2 : -Math.PI / 2);
					xElement += Math.cos(peakAngleToPerson) * b.getDamage() / distanceToLinePow2;
					yElement += Math.sin(peakAngleToPerson) * b.getDamage() / distanceToLinePow2;
				}
			}
		}
		for (SprayDrop sd : env.sprayDrops)
		{
			double distancePow2 = Methods.DistancePow2(this.x, this.y, sd.x, sd.y);
			if (distancePow2 < maximumDistanceICareAboutPow2)
			{
				boolean clockwise = (sd.x - this.x) * Math.sin(sd.angle()) > (sd.y - this.y) * Math.cos(sd.angle());
				double peakAngleToPerson = sd.angle() + (clockwise ? Math.PI / 2 : -Math.PI / 2);
				xElement += Math.cos(peakAngleToPerson) * sd.getDamage() / distancePow2;
				yElement += Math.sin(peakAngleToPerson) * sd.getDamage() / distancePow2;
			}
		}
		if (xElement == 0 && yElement == 0)
			return false;
		if (this.timeSinceLastInstinct >= this.instinctDelayTime)
		{
			this.timeSinceLastInstinct = -this.instinctDelayTime;
			return false;
		}
		if (this.timeSinceLastInstinct < 0)
			return false;
		// while under instincts, timeSinceLastInstinct == 0
		if (!considerAttempt)
		{
			this.directionOfAttemptedMovement = Math.atan2(yElement, xElement);
			this.angleOfLastInstinct = this.directionOfAttemptedMovement;
		} else // attempted movement would be half-decided by attempt
		{
			this.directionOfAttemptedMovement = Methods.meanAngle(Math.atan2(yElement, xElement), this.directionOfAttemptedMovement);
			this.angleOfLastInstinct = this.directionOfAttemptedMovement;
		}
		this.strengthOfAttemptedMovement = 1;
		this.timeSinceLastInstinct = 0;
		return true;
	}

}
