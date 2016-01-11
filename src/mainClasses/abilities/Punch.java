package mainClasses.abilities;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import mainClasses.Ability;
import mainClasses.Ball;
import mainClasses.EP;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Punch extends Ability
{
	final double	punchRotationSpeed	= 1.3;
	final int		squareSize			= 96;

	public Punch(int p)
	{
		super("Punch", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		range = (int) (1.15 * user.radius);

		/*
		 * Punch
		 */
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		// difference between user's rotation and angle:
		double tempAngleThing = Math.abs(user.rotation - angle) % (2 * Math.PI);
		double angleDifference = tempAngleThing > Math.PI ? 2 * Math.PI - tempAngleThing : tempAngleThing;
		if (user.z == 0)
		{
			if (!user.prone && !user.maintaining)
				if (cost <= user.stamina)
				{
					user.notMoving = stopsMovement; // returns to be False in hotkey();
					user.switchAnimation(0); // before punching the user simply rotates to the target direction
					if (cooldownLeft <= 0)
					{
						if (angleDifference < 0.2)
						{
							user.punchedSomebody = false;
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
					} else if ((user.animState == 5 || user.animState == 6) && user.animFrame < 2) // during a punch
					{
						if (!user.punchedSomebody)
							testUserPunch(user, env, false, false);
					}
				} else
					user.notMoving = false;
		} else if (user.z >= 1.1)
		{
			if (!user.prone && !user.maintaining && cost <= user.stamina) // maybe the previous if can be inserted after the identical if of the other one? dunno
			{
				/*
				 * Flight-punches have no cooldown, and instead of activating immediately when pressing they cause the user to glide downwards while moving (like what happens when you don't press any key while flying), except they stop going lower
				 * when they're at z = 1.1, and then they keep staying at the same level. When they are close enough to an enemy (and aiming at it) they will fist him (ha) and have their stamina reduced for the cost, as always.
				 */
				// remember: the flight-changing occurs on movePerson, using a test of user.powerRepetitivelyTryingToUse

				if (cooldownLeft == 0)
					user.punchedSomebody = false;

				if (!user.punchedSomebody)
					if (testUserPunch(user, env, true, false))
					{
						user.stamina -= cost;
						user.switchAnimation(11 + (user.lastHandUsedIsRight ? 0 : 1));
						cooldownLeft = cooldown;
					} else
						user.switchAnimation(10);

			}
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "look";
	}

	boolean testUserPunch(Person user, Environment env, boolean onlyOrganics, boolean missSound)
	{
		range = (int) (1.15 * user.radius);
		if (user.flySpeed != -1)
			range = range + 65; // eh
		double damage = user.STRENGTH;
		double pushback = user.STRENGTH * 1.5; // TODO have some cool power that greatly increases the pushback, thus making the user bounce off of walls!
		pushback += Math.sqrt(user.xVel * user.xVel + user.yVel * user.yVel) * 100 / 3000; // TODO uhhh

		// Notice:
		damage += 0.2 * pushback;
		pushback -= 0.2 * pushback;

		int damageType = 0; // TODO other punch types that aren't blunt?
		int timesTested = 2; // number of checks; number of parts the range is divided into.
		double extraVerticalHeight = 0.4;
		final double extraAimingAngle = user.flySpeed == -1 ? 0.3 : 0.8; // If the user is flying the arc of punch-testing is larger, because it's more difficult to aim with the keyboard and while moving.
		for (int l = 0; l < timesTested; l++)
		{
			range = (int) (user.radius * 1.15 * (1 - (double) l / timesTested));
			collisionCheck: if (true)
			{
				for (double m = -1; m <= 1; m++)
				{
					user.target = new Point((int) (user.x + range * Math.cos(user.rotation + extraAimingAngle * m)), (int) (user.y + range * Math.sin(user.rotation + extraAimingAngle * m)));
					if (!onlyOrganics)
					{
						int i = user.target.x / squareSize;
						int j = user.target.y / squareSize;
						if (env.wallTypes[i][j] != -1)
						{
							double leftoverPushback = env.wallHealths[i][j] > damage + pushback ? pushback : Math.min(env.wallHealths[i][j], pushback);
							env.damageWall(i, j, damage + pushback, damageType);
							env.hitPerson(user, damage, leftoverPushback, user.rotation - Math.PI, damageType);
							user.punchedSomebody = true;
							break collisionCheck;
						}
						// TODO arcforcefields
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
										double leftoverPushback = ff.life > damage + pushback ? pushback : Math.min(env.wallHealths[i][j], pushback);
										env.damageFF(ff, damage + pushback, user.target);
										env.hitPerson(user, damage, leftoverPushback, user.rotation - Math.PI, 4); // Shock damage
										user.punchedSomebody = true;
										break collisionCheck;
									}
								}
							}
						for (Ball b : env.balls)
							if (b.z + b.height > user.z && b.z < user.z + user.height)
								if (Methods.DistancePow2(new Point((int) b.x, (int) b.y), user.target) < b.radius * b.radius)
								{
									// Shatters the ball
									// TODO add ball reflecting using fists (so epic!)
									b.xVel = 0; // destroys ball
									b.yVel = 0; //
									env.hitPerson(user, b.getDamage(), pushback, user.rotation - Math.PI, EP.damageType(b.elementNum));
									// epicness
									env.ballDebris(b, "punch", b.angle());
									user.punchedSomebody = true;
									break collisionCheck;
								}
					}
					for (Person p : env.people)
						// allowing higher vertical range, for flying people? //TODO leave it as it is or remove it and lower punch-flight height to about 0.6
						if (p.z + p.height > user.z - extraVerticalHeight && p.z < user.z + user.height + extraVerticalHeight)
							// This is actually the purpose of the punches, by the way
							if (!p.equals(user)) // TODO find a better way (not two double-number comparisons) to make sure they aren't the same. Maybe with person.equals()?
								if (p.x - p.radius / 2 < user.target.x && p.y - p.radius / 2 < user.target.y && p.x + p.radius / 2 > user.target.x && p.y + p.radius / 2 > user.target.y)
								{
									env.hitPerson(p, damage, pushback, user.rotation, damageType); // This is such an elegant line of code :3
									user.punchedSomebody = true;
									break collisionCheck;
								}
				}
			}
		}
		if (user.punchedSomebody)
		{
			// backwards pushback
			env.hitPerson(user, 0, pushback * 0.6, user.rotation + Math.PI, 0);
			// Sound effect of hit
			playSound("Punch hit");
			return true;
		} else
		{
			if (missSound)
				playSound("Punch miss");
			return false;
		}
	}
}
