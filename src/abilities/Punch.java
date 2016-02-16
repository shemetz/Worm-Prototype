package abilities;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Ball;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainResourcesPackage.SoundEffect;

public class Punch extends Ability
{
	final double punchRotationSpeed = 1.3;
	final int squareSize = 96;

	public Punch(int p)
	{
		super("Punch", p);
		cost = 0.7;
		costType = CostType.STAMINA;
		cooldown = 0.55; // is actually 0.55 - Math.min(0.02*FITNESS, 0.15);
		rangeType = RangeType.EXACT_RANGE;
		stopsMovement = true;
		instant = true;
		// range = (int) (1.15 * radius); //in person's
		natural = true;

		sounds.add(new SoundEffect("Punch_hit_1.wav"));
		sounds.add(new SoundEffect("Punch_hit_2.wav"));
		sounds.add(new SoundEffect("Punch_hit_3.wav"));
		sounds.add(new SoundEffect("Punch_miss_1.wav"));
		sounds.add(new SoundEffect("Punch_miss_2.wav"));
		sounds.add(new SoundEffect("Punch_miss_3.wav"));
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		range = (int) (2.3 * user.radius);

		/*
		 * Punch
		 */
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		// difference between user's rotation and angle:
		double tempAngleThing = Math.abs(user.rotation - angle) % (2 * Math.PI);
		double angleDifference = tempAngleThing > Math.PI ? 2 * Math.PI - tempAngleThing : tempAngleThing;
		if (user.z == 0)
		{
			if (!user.prone && (!user.maintaining || user.abilities.get(user.abilityMaintaining) instanceof Sprint)) // special enable for sprint-punching
				if (cost <= user.stamina)
				{
					user.notMoving = stopsMovement; // returns to be False in hotkey();
					user.switchAnimation(0); // before punching the user simply rotates to the target direction
					if (cooldownLeft <= 0)
					{
						if (angleDifference < 0.2)
						{
							user.punchedSomething = false;
							angle = angle - user.missAngle + 2 * user.missAngle * Math.random(); // possibility to miss with a punch, of course.
							user.rotation = angle;
							testUserPunch(user, env, false, true);

							user.switchAnimation(5);
							user.stamina -= cost;
							cooldownLeft = cooldown;
						}
						// Rotate towards target anyways
						else
						{
							if (user.z == 0)
								user.rotate(angle, 0.02 * punchRotationSpeed);
							else
								user.rotate(angle, 0.02 * 3 * punchRotationSpeed);
						}
					}
					else if ((user.animState == 5 || user.animState == 6) && user.animFrame < 2) // during a punch
					{
						if (!user.punchedSomething)
							testUserPunch(user, env, false, false);
					}
				}
				else
					user.notMoving = false;
		}
		else if (user.z >= 1.1 && user.flySpeed > 0) // Fly, flight punch
		{
			if (!user.prone && !user.maintaining && cost <= user.stamina) // maybe the previous if can be inserted after the identical if of the other one? dunno
			{
				/*
				 * Flight-punches have no cooldown, and instead of activating immediately when pressing they cause the user to glide downwards while moving (like what happens when you don't press any key while flying), except they stop going lower
				 * when they're at z = 1.1, and then they keep staying at the same level. When they are close enough to an enemy (and aiming at it) they will fist him (ha) and have their stamina reduced for the cost, as always.
				 */
				// remember: the flight-changing occurs on movePerson, using a test of user.powerRepetitivelyTryingToUse

				if (cooldownLeft == 0)
					user.punchedSomething = false;

				if (!user.punchedSomething)
					if (testUserPunch(user, env, true, false))
					{
						user.stamina -= cost;
						user.switchAnimation(11 + (user.lastHandUsedIsRight ? 0 : 1));
						cooldownLeft = cooldown;
					}
					else
						user.switchAnimation(10);

			}
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
	}

	boolean testUserPunch(Person user, Environment env, boolean onlyOrganics, boolean missSound)
	{
		range = (int) (2.3 * user.radius);
		if (user.flySpeed != -1)
			range = range + 65; // eh
		double damage = user.STRENGTH * 1;
		double pushback = user.STRENGTH * 1.5; // TODO have some cool power that greatly increases the pushback, thus making the user bounce off of walls!
		pushback += Math.sqrt(user.xVel * user.xVel + user.yVel * user.yVel) * 100 / 3000; // TODO uhhh

		// Notice:
		damage += 0.2 * pushback;
		pushback -= 0.2 * pushback;
		if (user.timeEffect > 1)
		{
			// I would have made it work even if timeEfect < 1, but that's OP with the fact that fists are instantaneous (unlike in the real world).
			// TODO maybe later, add a delay to the punch, and the delay will depend on timeEffect, so this will be better.
			damage /= user.timeEffect;
			pushback /= user.timeEffect;
		}

		int punchElement = -1; // TODO other punch types that aren't blunt?
		int punchDamageType = 0;
		int timesTested = 2; // number of checks; number of parts the range is divided into.
		double extraVerticalHeight = 0.4;
		final double extraAimingAngle = user.flySpeed == -1 ? 0.3 : 0.8; // If the user is flying the arc of Punch-testing is larger, because it's more difficult to aim with the keyboard and while moving.
		for (int l = 0; l < timesTested; l++)
		{
			range = (int) (user.radius * 2.3 * (1 - (double) l / timesTested));
			collisionCheck: if (true)
			{
				for (double m = -1; m <= 1; m++)
				{
					user.target = new Point((int) (user.x + range * Math.cos(user.rotation + extraAimingAngle * m)), (int) (user.y + range * Math.sin(user.rotation + extraAimingAngle * m)));
					if (!onlyOrganics)
					{
						int i = user.target.x / squareSize;
						int j = user.target.y / squareSize;
						if (env.wallTypes[i][j] > 0)
						{
							int wallHealth = env.wallHealths[i][j];
							double leftoverPushback = wallHealth > damage + pushback ? pushback : Math.min(wallHealth, pushback);
							env.damageWall(i, j, damage + pushback, punchDamageType);
							env.hitPerson(user, Math.max(0, 7 - 0.5 * user.STRENGTH), leftoverPushback, user.rotation - Math.PI, env.wallTypes[i][j]); // When punching walls, you damage yourself by 7 damage points.
							user.punchedSomething = true;
							break collisionCheck;
						}
						for (ForceField ff : env.FFs)
							if (ff.z + ff.height > user.z && ff.z < user.z + user.height)
							{
								Rectangle2D ffBoundingBox = new Rectangle2D.Double(ff.x - ff.length / 2, ff.y - ff.length / 2, ff.length, ff.length);
								if (ffBoundingBox.contains(user.target))
								{
									// Note: this uses ff.width + 20 because the regular width is sometimes too thin and the punch is on the other side so it isn't recognized
									Area forcefieldArea = new Area(new Rectangle2D.Double(ff.x - ff.length / 2, ff.y - ff.width / 2 - 20 / 2, ff.length, ff.width + 20));
									AffineTransform aft = new AffineTransform();
									aft.rotate(ff.rotation, ff.x, ff.y);
									forcefieldArea.transform(aft);
									if (forcefieldArea.contains(user.target))
									{
										double leftoverPushback = ff.life > damage + pushback ? pushback : Math.min(ff.life, pushback);
										env.damageForceField(ff, damage + pushback, user.target);
										env.hitPerson(user, ff.armor, leftoverPushback, user.rotation - Math.PI, 6); // Energy damage, equal to the armor of the force field.
										user.punchedSomething = true;
										break collisionCheck;
									}
								}
							}

						if (!user.ghostMode)
							for (Ball b : env.balls)
								if (b.z + b.height > user.z && b.z < user.z + user.height)
									if (Methods.DistancePow2(new Point((int) b.x, (int) b.y), user.target) < b.radius * b.radius)
									{
										// Shatters the ball
										// TODO add ball reflecting using fists (so epic!)
										b.xVel = 0; // destroys ball
										b.yVel = 0; //
										env.hitPerson(user, b.getDamage(), pushback, user.rotation - Math.PI, b.elementNum);
										// epicness
										env.ballDebris(b, "punch", b.angle());
										user.punchedSomething = true;
										break collisionCheck;
									}
					}
					for (ArcForceField aff : env.AFFs)
						if (!user.ghostMode || aff.type.equals("bubble"))
						{
							if (aff.target.equals(user) && aff.type != "Bubble")
								continue;
							if (aff.highestPoint() < user.z || aff.z > user.highestPoint())
								continue;
							if (Methods.DistancePow2(user.target, aff.target.Point()) > aff.maxRadius * aff.maxRadius) // outer arc
								continue;
							double extra = 30; // extra "inwards" distance
							if (Methods.DistancePow2(user.target, aff.target.Point()) < Math.pow(aff.minRadius - extra, 2)) // inner arc
								continue;
							boolean withinAngles = false;
							if (aff.arc == 2 * Math.PI)
								withinAngles = true;
							else
							{
								double angleToPunch = Math.atan2(user.target.y - aff.y, user.target.x - aff.x);
								while (aff.rotation < -Math.PI)
									aff.rotation += 2 * Math.PI;
								while (aff.rotation >= Math.PI)
									aff.rotation -= 2 * Math.PI;
								double minAngle = aff.rotation - aff.arc / 2;
								double maxAngle = aff.rotation + aff.arc / 2;
								if (angleToPunch > minAngle && angleToPunch < maxAngle)
									withinAngles = true;
							}
							if (withinAngles) // check angles
							{
								double leftoverPushback = aff.life > damage + pushback ? pushback : Math.min(aff.life, pushback);
								env.damageArcForceField(aff, damage + pushback, user.target, 0);
								int AFFelement = aff.elementNum;
								if (AFFelement == 12)
									AFFelement = 6; // energy
								env.hitPerson(user, 5, leftoverPushback, user.rotation - Math.PI, AFFelement); // 5 damage. TODO
								user.punchedSomething = true;
								break collisionCheck;
							}
						}
					for (Person p : env.people)
						if (user.ghostMode == p.ghostMode)
							// allowing higher vertical range, for flying people? //TODO leave it as it is or remove it and lower Punch-flight height to about 0.6
							if (p.highestPoint() > user.z - extraVerticalHeight && p.z < user.highestPoint() + extraVerticalHeight)
								// This is actually the purpose of the punches, by the way
								if (!p.equals(user))
									if (p.x - p.radius < user.target.x && p.y - p.radius < user.target.y && p.x + p.radius > user.target.x && p.y + p.radius > user.target.y)
									{
										env.hitPerson(p, damage, pushback, user.rotation, punchElement); // This is such an elegant line of code :3
										user.punchedSomething = true;
										break collisionCheck;
									}
				}
			}
		}
		// restore range to normal
		range = (int) (2.3 * user.radius);
		if (user.punchedSomething)
		{
			// backwards pushback
			env.hitPerson(user, 0, pushback * 0.6, user.rotation + Math.PI, -1);
			// Sound effect of hit
			sounds.get((int) (Math.random() * 3)).play();
			return true;
		}
		else
		{
			if (missSound) // sound effect of miss
				sounds.get((int) (Math.random() * 3) + 3).play();
			return false;
		}
	}
}
