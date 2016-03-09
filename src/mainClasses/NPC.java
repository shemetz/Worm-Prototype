package mainClasses;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import abilities.Punch;
import pathfinding.AStarPathFinder;
import pathfinding.EnvMap;
import pathfinding.Mover;
import pathfinding.Path;
import pathfinding.WayPoint;

public class NPC extends Person
{
	public Strategy strategy;

	public enum Strategy
	{
		AGGRESSIVE, DEFENSIVE, PASSIVE, POSSESSED, CLONE
	}
	// AGGRESSIVE = attack enemies if possible, then heal/buff and follow if possible.
	// DEFENSIVE = push away enemies and block them if possible, run away if possible.
	// PASSIVE = does nothing.
	// CLONE_I = attacks closest enemy in 8 meters range, follow master otherwise

	Tactic tactic;

	enum Tactic
	{
		CIRCLE_STRAFING, NO_TARGET, RETREAT, PANIC, CHASING, USE_TARGETING_ABILITY, PUNCH
	};
	// CIRCLE_STRAFING = move in a constant angular direction around the target, shooting at it when able. When hitting something or randomly while moving, change direction.
	// NO_TARGET = no target. Do nothing, wait to find a tactic.
	// RETREAT = move away from any person within retreat range. Use a helpful escape ability if possible.
	// PANIC = run aimlessly, not stopping, randomly rotating (panic).
	// PUNCH_CHASING = move towards the target, and punch them when able.
	// USE_TARGETING_ABILITY = use an ability that targets the enemy

	boolean hasAllies = false;

	int targetID = -1;
	boolean rightOrLeft = false; // true = right or CW. false = left or CCW.
	boolean justCollided = false;
	public double instinctDelayTime;
	double timeSinceLastInstinct;
	double angleOfLastInstinct;
	double timeSinceLastDistCheck;
	public double maximumDistanceICareAboutPow2;
	double lastDistPow2 = Double.MAX_VALUE;
	List<WayPoint> path;
	EnvMap envMap = null;
	AI ai;

	// stuff for tactics?
	double noCircleStrafeTimer = 0;

	public NPC(double x1, double y1, Strategy s1)
	{
		super(x1, y1);
		// TEMP
		hasAllies = false;
		strategy = s1;
		tactic = Tactic.NO_TARGET;
		timeSinceLastInstinct = instinctDelayTime;
		angleOfLastInstinct = 0;
		timeSinceLastDistCheck = 0;
		path = new ArrayList<WayPoint>();
		rename(); // random npc name - no abilities yet
		ai = null;
	}

	public void updateSubStats()
	{
		basicUpdateSubStats();
		instinctDelayTime = Math.max(0.15 - 0.015 * WITS, 0); // average is 0.35 secs
		maximumDistanceICareAboutPow2 = Math.pow(WITS * 70, 2);
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
		if (enemy.z >= z + height) // TODO remove this when ground-to-air combat is more possible
			return false;
		if (enemy.dead)
			return false;

		return true;
	}

	int frameNum = 0;

	void frameAIupdate(double deltaTime, Environment env, MAIN main)
	{
		if (ai == null)
			ai = new AI(this, env, main);
		frameNum++;
		// Refresh environment map every 0.5 seconds
		if (frameNum % 25 == 0 || envMap == null)
			refreshMap(env);

		// ~React to situation~

		// Use all defensive instant abilities if being hit (e.g. Protective Bubble)
		if (this.timeSinceLastHit == 0 && this.strategy != Strategy.POSSESSED)
			for (int aIndex = 0; aIndex < this.abilities.size(); aIndex++)
			{
				Ability a = this.abilities.get(aIndex);
				if (a.hasTag("defensive") && a.instant)
				{
					main.pressAbilityKey(aIndex, true, this); // activate defensive ability
					this.abilityTryingToRepetitivelyUse = -1;
				}
			}

		// Choose as target the closest viable target (closest in path)
		if (frameNum % 25 == 0) // don't check a lot of times in a short period
		{
			// Choose as target the closest enemy (considering pathfinding).
			List<Person> possibleTargets = new ArrayList<Person>();
			for (Person p2 : env.people)
				if (this.viableTarget(p2))
					possibleTargets.add(p2);
			if (!possibleTargets.isEmpty())
			{
				int bestTargetIndex = 0;
				double shortestPathLength = Integer.MAX_VALUE;
				for (int i = 0; i < possibleTargets.size(); i++)
				{
					List<WayPoint> pathToTarget = pathFind(possibleTargets.get(i).Point());
					if (pathToTarget != null) // it's null if it's impossible
					{
						double pathLength = pathLength(pathToTarget);
						if (pathLength < shortestPathLength)
						{
							shortestPathLength = pathLength;
							bestTargetIndex = i;
						}
					}
				}
				this.targetID = possibleTargets.get(bestTargetIndex).id;
			}
			else
				;// NPCs will just punch the corpse of the last person they attacked. I guess that's fine.
		}
		Person targetPerson = null;
		if (this.targetID != -1)
			for (Person p2 : env.people)
				if (p2.id == this.targetID)
				{
					targetPerson = p2;
					break;
				}

		// tactic-switching decisions. TODO make it make sense
		double distanceToTargetPow2 = targetPerson == null ? -1 : Methods.DistancePow2(this.x, this.y, targetPerson.x, targetPerson.y);
		Tactic prevTactic = this.tactic;
		tacticSearch: switch (this.strategy)
		{
		case POSSESSED:
			this.tactic = Tactic.NO_TARGET;
			break;
		case PASSIVE:
			// Panic if panicking
			if (this.panic)
			{
				this.tactic = Tactic.PANIC;
				break tacticSearch;
			}

			// Retreat if life is less than 25% of max life
			if (targetPerson != null)
				if (this.life < 0.25 * this.maxLife)
				{
					this.tactic = Tactic.RETREAT;
					break tacticSearch;
				}
			break;
		case AGGRESSIVE:
			// Panic if panicking
			if (this.panic)
			{
				this.tactic = Tactic.PANIC;
				break tacticSearch;
			}
			// Retreat if life is less than 15% of max life
			if (targetPerson != null)
				if (this.life < 0.15 * this.maxLife)
				{
					this.tactic = Tactic.RETREAT;
					break tacticSearch;
				}

			// no break; on PURPOSE
		case CLONE:

			// Circle-strafe around target if there exists a useable projectile/beam power
			if (targetPerson != null)
				if (noCircleStrafeTimer <= 0)
					for (Ability a : this.abilities)
						if (a.hasTag("projectile") || a.hasTag("beam"))
							if (!a.disabled)
							{
								if (a.costPerSecond < this.mana) // if 1 second cost is less than available mana
								{
									this.tactic = Tactic.CIRCLE_STRAFING;
									break tacticSearch;
								}
								else
									noCircleStrafeTimer = 15; // 15 seconds of not using this tactic
							}

			// Chase target if target isn't in punch range
			Ability punch = null;
			for (Ability a : this.abilities)
				if (a instanceof Punch)
					punch = a;
			if (targetPerson != null)
				if (distanceToTargetPow2 >= punch.range * punch.range)
				{
					this.tactic = Tactic.CHASING;
					break tacticSearch;
				}
				else
				{
					this.tactic = Tactic.PUNCH;
					break tacticSearch;
				}
			this.tactic = Tactic.NO_TARGET;
			break tacticSearch;
		default:
			MAIN.errorMessage("No code found for strategy named   " + this.strategy);
			break;
		}

		if (prevTactic != this.tactic)
		{
			if (this.abilityMaintaining != -1)
				main.pressAbilityKey(this.abilityMaintaining, false, this);
			this.abilityMaintaining = -1;
			this.abilityAiming = -1;
			this.abilityTryingToRepetitivelyUse = -1;
		}

		// Do the AI tactic chosen
		switch (this.tactic)
		{
		case CHASING:
			ai.CHASE(targetPerson, deltaTime);
			break;
		case CIRCLE_STRAFING:
			ai.CIRCLE_STRAFE(targetPerson, deltaTime);
			break;
		case RETREAT:
			ai.RETREAT(targetPerson, deltaTime);
			break;
		case PANIC:
			ai.PANIC(deltaTime);
			break;
		case PUNCH:
			ai.PUNCH(targetPerson);
			break;
		case NO_TARGET:
			strengthOfAttemptedMovement = 0;
			directionOfAttemptedMovement = 0;
			break;
		default:
			MAIN.errorMessage("6j93k, no tactic - " + this.tactic);
			break;
		}

		// resetting check-booleans
		this.justCollided = false;

		// decreasing some timers
		if (noCircleStrafeTimer > 0)
			noCircleStrafeTimer -= deltaTime;

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
		if (this.strategy != Strategy.POSSESSED)
			if (moveAwayFromDangerousObjects(env, !nothingToDo))
				if (nothingToDo)
					this.rotate(this.directionOfAttemptedMovement, deltaTime);
				else if (this.timeSinceLastInstinct >= 0 && this.timeSinceLastInstinct < this.instinctDelayTime)
				{
					// for a period of instinctDelayTime after finishing with instincts, keep moving
					this.strengthOfAttemptedMovement = 1;
					this.timeSinceLastInstinct += deltaTime;
				}

		// Group tactics - stay a bit away from any non-enemy
		double forceX = this.strengthOfAttemptedMovement * Math.cos(this.directionOfAttemptedMovement), forceY = this.strengthOfAttemptedMovement * Math.sin(this.directionOfAttemptedMovement);
		for (Person p : env.people)
			if (!p.equals(this) && !this.viableTarget(p))
			{
				double angle = Math.atan2(this.y - p.y, this.x - p.x);
				// ~5000 = what you'd expect, but leads to many problems (people running into sideways walls).
				double amount = 1000 / Methods.DistancePow2(p.Point(), this.Point());
				forceX += amount * Math.cos(angle);
				forceY += amount * Math.sin(angle);
			}
		this.directionOfAttemptedMovement = Math.atan2(forceY, forceX);
	}

	void refreshMap(Environment env)
	{
		envMap = new EnvMap(env);
	}

	List<WayPoint> pathFind(Point targetPoint)
	{
		AStarPathFinder pathFinder = new AStarPathFinder(envMap, 50, false);

		targetPoint.x = Math.max(0, targetPoint.x);
		targetPoint.x = Math.min(envMap.width * 96, targetPoint.x);
		targetPoint.y = Math.max(0, targetPoint.y);
		targetPoint.y = Math.min(envMap.height * 96, targetPoint.y);
		Path foundPath = pathFinder.findPath(this, (int) (x / 96), (int) (y / 96), targetPoint.x / 96, targetPoint.y / 96);
		if (foundPath != null)
		{
			// transform into a list of grid points
			List<WayPoint> blargl = new ArrayList<WayPoint>();
			double possibleMovement = Math.sqrt(1 + 1); // obvs

			blargl.add(new WayPoint(foundPath.getX(0), foundPath.getY(0)));
			for (int i = 1; i < foundPath.getLength(); i++)
			{
				WayPoint wp = new WayPoint(foundPath.getX(i), foundPath.getY(i));
				// Is it through a portal?
				if (Math.pow(foundPath.getX(i - 1) - foundPath.getX(i), 2) + Math.pow(foundPath.getX(i - 1) - foundPath.getX(i), 2) > possibleMovement * possibleMovement)
				{
					// set the byPortal of the previous one to true
					blargl.get(blargl.size() - 1).byPortal = true;
				}
				blargl.add(wp);
			}

			double minDistancePow2 = Math.pow(radius, 2);

			// Refine bestPath

			// 1: Merge every two adjacent DIFFERENT lines to one diagonal line if possible
			int prevPathLength;
			do
			{
				prevPathLength = blargl.size();
				bigloop: for (int i = 0; i < blargl.size() - 2; i++)
				{
					Point A = blargl.get(i);
					Point B = blargl.get(i + 1);
					Point C = blargl.get(i + 2);
					// check if points are not on same line
					// if AC is horizontal
					if (A.x == C.x && B.x == C.x)
						continue bigloop;
					// if AC is vertical.
					if (A.y == C.y && B.y == C.y)
						continue bigloop;
					// match the gradients
					if ((A.x - C.x) * (A.y - C.y) == (C.x - B.x) * (C.y - B.y))
						continue bigloop;
					// not through a Portal
					if (blargl.get(i + 1).byPortal)
						continue bigloop;
					boolean OKToMerge = true;
					int minX = Math.min(A.x, Math.min(B.x, C.x));
					int maxX = Math.max(A.x, Math.max(B.x, C.x));
					int minY = Math.min(A.y, Math.min(B.y, C.y));
					int maxY = Math.max(A.y, Math.max(B.y, C.y));
					// check if it's OK to merge
					loop: for (int xx = minX; xx <= maxX; xx++)
						for (int yy = minY; yy <= maxY; yy++)
							if (envMap.getCost(this, A.x, A.y, xx, yy) > 1)
								if (Methods.LineToPointDistancePow2(A, C, new Point(xx, yy)) < minDistancePow2)
								{
									OKToMerge = false;
									break loop;
								}
					if (OKToMerge)
					{
						blargl.remove(i + 1);
						i--;
					}
				}

			}
			while (prevPathLength > blargl.size());
			// 2: Merge every three points on the same line into two points
			do
			{
				prevPathLength = blargl.size();
				for (int i = 0; i < blargl.size() - 2; i++)
				{
					Point A = blargl.get(i);
					Point B = blargl.get(i + 1);
					Point C = blargl.get(i + 2);
					// check if points are on same line
					boolean OKToMerge = false;
					// if AC is horizontal
					if (A.x == C.x && B.x == C.x)
						OKToMerge = true;
					// if AC is vertical.
					if (A.y == C.y && B.y == C.y)
						OKToMerge = true;
					// match the gradients
					if ((A.x - C.x) * (A.y - C.y) == (C.x - B.x) * (C.y - B.y))
						OKToMerge = true;
					// not through a Portal
					if (blargl.get(i + 1).byPortal)
						OKToMerge = false;
					if (OKToMerge)
					{
						blargl.remove(i + 1);
						i--;
					}
				}

			}
			while (prevPathLength > blargl.size());

			// transform into a list of points
			List<WayPoint> bestPath = new ArrayList<WayPoint>();
			for (int i = 0; i < blargl.size(); i++)
			{
				bestPath.add(new WayPoint(blargl.get(i).x * 96 + 96 / 2, blargl.get(i).y * 96 + 96 / 2, blargl.get(i).byPortal));
			}
			return bestPath;
		}
		else
			return null; // no path possible
	}

	void updatePath(Environment env)
	{
		if (path != null && !path.isEmpty())
		{
			WayPoint waypoint = path.get(0);
			// remove first point if in same tile
			if ((int) (waypoint.x / 96) == (int) (x / 96) && (int) (waypoint.y / 96) == (int) (y / 96))
				if (waypoint.byPortal == false)
					path.remove(0);
				else if (!waypoint.portalWasUsed)
				{
					// find relevant portal
					Portal portal = null;
					for (Portal p : env.portals)
						if (Methods.LineToPointDistancePow2(p.start, p.end, waypoint) < 96 * 96 * 2)
							if (Methods.DistancePow2(p.x, p.y, waypoint.x, waypoint.y) < p.length * p.length)
								portal = p;
					if (portal == null)
					{
						MAIN.errorMessage("	");
						return;
					}

					// move waypoint to other side of *same* portal, like a mirror, to make the NPC move towards it.
					double prevAngle = Math.atan2(this.y - portal.y, this.x - portal.x);
					double newAngle = portal.angle - (prevAngle - portal.angle);
					double distToWayPoint = Math.sqrt(Methods.DistancePow2(waypoint.x, waypoint.y, portal.x, portal.y));
					waypoint.x = (int) (portal.x + distToWayPoint * Math.cos(newAngle));
					waypoint.y = (int) (portal.y + distToWayPoint * Math.sin(newAngle));
					waypoint.portalWasUsed = true;
				}
				else if (Methods.DistancePow2(waypoint, Point()) > 96 * 96 * 4) // sort of right
				{
					MAIN.print("this is supposed to happen");
					path.remove(0);
				}
		}
	}

	int pathLength(List<WayPoint> p)
	{
		if (p == null || p.isEmpty())
			return 0;

		int sum = 0;
		for (int i = 0; i < p.size() - 1; i++)
			sum += Math.sqrt(Methods.DistancePow2(p.get(i + 1).x, p.get(i + 1).y, p.get(i).x, p.get(i).y));
		return sum;
	}

	Mover Mover()
	{
		class NPCMover implements Mover
		{
			public NPCMover()
			{
			}
		}
		return new NPCMover();
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
		}
		else // attempted movement would be half-decided by attempt
		{
			this.directionOfAttemptedMovement = Methods.meanAngle(Math.atan2(yElement, xElement), this.directionOfAttemptedMovement);
			this.angleOfLastInstinct = this.directionOfAttemptedMovement;
		}
		this.strengthOfAttemptedMovement = 1;
		this.timeSinceLastInstinct = 0;
		return true;
	}

}
