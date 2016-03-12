package abilities;

import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import effects.Nullified;
import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Ball;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Furniture;
import mainClasses.MAIN;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainResourcesPackage.SoundEffect;

public class Punch extends Ability
{
	final double punchRotationSpeed = 1.3;
	final int squareSize = 96;
	final double timeNotMovingAfterPunch = 0.3;

	public Punch(int p)
	{
		super("Punch", p);
		cost = 0.7;
		costType = CostType.STAMINA;
		cooldown = 0.55; // is actually 0.55 - Math.min(0.02*FITNESS, 0.15);
		rangeType = RangeType.EXACT_RANGE;
		stopsMovement = true;
		instant = true;
		range = 6174; // will be recalculated the moment this ability is added in Person.something()
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
		range = (int) (2.3 * user.radius);

		/*
		 * Punch
		 */
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		// difference between user's rotation and angle:
		double tempAngleThing = Math.abs(user.rotation - angle) % (2 * Math.PI);
		double angleDifference = tempAngleThing > Math.PI ? 2 * Math.PI - tempAngleThing : tempAngleThing;

		if (user.flySpeed == -1 && user.z == 0 || user.z == 1)
		{
			if (user.maintaining && user.abilityMaintaining == -1)
			{
				// BUG !!!!!! THIS CODE IS NOT PERMANENT IT IS TEMPORARY
				MAIN.errorMessage("shvbypxuh .. . ?  " + user.name + "   " + user.abilities + " " + user.abilityTryingToRepetitivelyUse);
			}
			if (!user.prone && (!user.maintaining || user.abilities.get(user.abilityMaintaining) instanceof Sprint)) // special enable for sprint-punching
				if (cost <= user.stamina)
				{
					user.notMovingTimer = timeNotMovingAfterPunch;
					user.switchAnimation(0); // before punching the user simply rotates to the target direction
					if (cooldownLeft <= 0)
					{
						for (int i = 0; i < user.punchAffectingAbilities.size(); i++)
							if (user.punchAffectingAbilities.get(i) instanceof Strike_E && !user.punchAffectingAbilities.get(i).on)
							{
								user.punchAffectingAbilities.remove(i);
								i--;
							}
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
		damage = user.STRENGTH * 1;
		pushback = user.STRENGTH * 1.5;
		pushback += Math.sqrt(user.xVel * user.xVel + user.yVel * user.yVel) * 100 / 3000; // TODO uhhh
		double originalPushback = 0 + pushback;
		for (Ability a : user.punchAffectingAbilities)
		{
			if (a instanceof Pushy_Fists)
				pushback *= 2;

		}

		// Notice:
		damage += 0.2 * pushback;
		pushback -= 0.2 * pushback;
		double timeEffect = Math.min(user.timeEffect, 1);
		// I would have made it work even if timeEfect < 1, but that's OP with the fact that fists are instantaneous (unlike in the real world).
		// TODO maybe later, add a delay to the punch, and the delay will depend on timeEffect, so this will be better.
		damage /= timeEffect;
		pushback /= timeEffect;

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
						// Walls
						int i = Math.min(Math.max(user.target.x / squareSize, 0), env.width - 1);
						int j = Math.min(Math.max(user.target.y / squareSize, 0), env.height - 1);
						if (user.z < 1 && env.wallTypes[i][j] > 0)
						{
							for (Ability a : user.punchAffectingAbilities)
							{
								if (a instanceof Shattering_Fists)
									if (Math.random() < 0.25 * a.level)
									{
										env.nonRecursiveDamageWall(i, j, (damage + pushback) / timeEffect * 10, punchDamageType);
										a.sounds.get(0).play();
									}
								if (a instanceof Strike_E)
									env.damageWall(i, j, (a.damage + a.pushback) / timeEffect, a.elementNum);
							}
							env.damageWall(i, j, damage + pushback, punchDamageType);
							if (env.wallHealths[i][j] > 0)
								env.personPunchWall(user, 5, env.wallTypes[i][j]); // 5 damage? TODO
							user.punchedSomething = true;
							break collisionCheck;
						}
						// Furniture
						if (user.z < 1)
							for (Furniture f : env.furniture)
							{
								Rectangle2D fBoundingBox = new Rectangle2D.Double(f.x - f.w / 2, f.y - f.w / 2, f.w, f.w);
								if (fBoundingBox.contains(user.target))
								{
									// Note: this uses f.h + 20 because the regular h is sometimes too thin and the punch is on the other side so it isn't recognized
									Area fArea = new Area(new Rectangle2D.Double(f.x - f.w / 2, f.y - f.h / 2 - 20 / 2, f.w, f.h + 20));
									AffineTransform aft = new AffineTransform();
									aft.rotate(f.rotation, f.x, f.y);
									fArea.transform(aft);
									if (fArea.contains(user.target))
									{
										for (Ability a : user.punchAffectingAbilities)
										{
											if (a instanceof Shattering_Fists)
												if (Math.random() < 0.25 * a.level)
												{
													damage *= 10;
													a.sounds.get(0).play();
												}
											if (a instanceof Strike_E)
												damage += (a.damage + a.pushback) / timeEffect; // together why not
										}
										// make sure life > 0
										double leftoverPushback = (f.life > damage + pushback) ? pushback : Math.min(f.life, pushback);
										env.damageFurniture(f, damage + pushback, punchElement);
										env.hitPerson(user, f.armor, leftoverPushback, user.rotation - Math.PI, -1); // blunt damage, equal to the armor of the force field.
										user.punchedSomething = true;
										break collisionCheck;
									}
								}
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
										for (Ability a : user.punchAffectingAbilities)
										{
											if (a instanceof Shattering_Fists)
												if (Math.random() < 0.25 * a.level)
												{
													damage *= 10;
													a.sounds.get(0).play();
												}
											if (a instanceof Strike_E)
												damage += (a.damage + a.pushback) / timeEffect; // together why not
										}
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
						if (!user.ghostMode || aff.type == ArcForceField.Type.MOBILE_BUBBLE || aff.type == ArcForceField.Type.IMMOBILE_BUBBLE)
						{
							if (aff.target.equals(user) && aff.type != ArcForceField.Type.IMMOBILE_BUBBLE)
								continue;
							if (aff.highestPoint() < user.z || aff.z > user.highestPoint())
								continue;
							if (Methods.DistancePow2(user.target, aff.Point()) > aff.maxRadius * aff.maxRadius) // outer arc
								continue;
							double extra = 30; // extra "inwards" distance
							if (Methods.DistancePow2(user.target, aff.Point()) < Math.pow(aff.minRadius - extra, 2)) // inner arc
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
								for (Ability a : user.punchAffectingAbilities)
								{
									if (a instanceof Shattering_Fists)
										if (Math.random() < 0.25 * a.level)
										{
											damage *= 10;
											a.sounds.get(0).play();
										}
									if (a instanceof Strike_E)
										damage += (a.damage + a.pushback) / timeEffect; // together why not
								}
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
							if (p.highestPoint() > user.z - extraVerticalHeight && p.z < user.highestPoint() + extraVerticalHeight)
								if (!p.equals(user))
									if (p.x - p.radius < user.target.x && p.y - p.radius < user.target.y && p.x + p.radius > user.target.x && p.y + p.radius > user.target.y)
									{
										env.hitPerson(p, damage, pushback, user.rotation, punchElement); // This is such an elegant line of code :3

										for (Ability a : user.punchAffectingAbilities)
										{
											if (a instanceof Sapping_Fists)
												if (Math.random() < 0.1 * a.level) // 10% * level
												{
													p.affect(new Nullified(1, true, a), true);
													a.sounds.get(0).play();
												}
											if (a instanceof Elemental_Fists_E)
												env.hitPerson(p, a.damage / timeEffect, a.pushback / timeEffect, user.rotation, a.elementNum);
											if (a instanceof Strike_E)
											{
												// apply effect
												double[] dmgpush = env.trySpecialEffectReturnDamageAndPushback(p, a.elementNum, a.damage, a.pushback, 1); // 1 = certain
												double damage2 = dmgpush[0];
												double pushback2 = dmgpush[1];
												// might apply effect twice in some cases - I don't want to try to solve this, I'm lazy
												env.hitPerson(p, damage2 / timeEffect, pushback2 / timeEffect, user.rotation, a.elementNum);
											}
											if (a instanceof Vampiric_Fists)
											{
												user.heal(0.2 * a.level * damage);
												a.sounds.get(0).play();
											}
										}

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
			env.hitPerson(user, 0, originalPushback * 0.6, user.rotation + Math.PI, -1);
			// Sound effect of hit
			sounds.get((int) (Math.random() * 3)).play();

			for (Ability a : user.punchAffectingAbilities)
			{
				if (a instanceof Explosive_Fists)
					env.createExplosion(user.target.x, user.target.y, user.z, a.radius, a.damage, a.pushback, -1);
				if (a instanceof Strike_E)
					((Strike_E) a).turnOff();
				if (a instanceof Strike_E || a instanceof Elemental_Fists_E || a instanceof Pushy_Fists)
					a.sounds.get(0).play();
			}
			return true;
		}
		else
		{
			for (Ability a : user.punchAffectingAbilities)
				if (a instanceof Strike_E)
					((Strike_E) a).turnOff();
			if (missSound) // sound effect of miss
				sounds.get((int) (Math.random() * 3) + 3).play();
			return false;
		}
	}
}
