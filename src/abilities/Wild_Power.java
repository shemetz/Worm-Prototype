package abilities;

import java.awt.Point;

import effects.Nullified;
import mainClasses.Ability;
import mainClasses.Ball;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;

public class Wild_Power extends Ability
{
	public double maxDistFromTargetedPoint = 100; // NOTE: USED IN DRAWAIM!

	public Wild_Power(int p)
	{
		super("Wild Power", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
	}
	
	public void updateStats()
	{
		cost = 4;
		cooldown = 8 - 0.5 * level;
		range = 500;
		
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public Object getTarget(Environment env, Point targetPoint)
	{
		Object target = null;
		double shortestDistPow2 = maxDistFromTargetedPoint * maxDistFromTargetedPoint;
		// ENEMY or ALLY or SELF
		for (Person p : env.people)
			if (!p.dead)
			{
				double distPow2 = Methods.DistancePow2(p.Point(), targetPoint);
				if (distPow2 < shortestDistPow2)
				{
					shortestDistPow2 = distPow2;
					target = p;
				}
			}
		// BALL
		for (Ball b : env.balls)
		{
			double distPow2 = Methods.DistancePow2(b.Point(), targetPoint);
			if (distPow2 < shortestDistPow2)
			{
				shortestDistPow2 = distPow2;
				target = b;
			}
		}
		// WALL or POOL or FLOOR
		if (target == null)
		{
			int x = targetPoint.x / Environment.squareSize;
			int y = targetPoint.y / Environment.squareSize;
			target = new Point(x, y);
		}
		return target;
	}

	public void use(Environment env, Person user, Point target1)
	{
		if (user.mana >= cost && cooldownLeft <= 0)
		{
			user.mana -= cost;
			cooldownLeft = cooldown;
			int rand = (int) (Math.random() * 3); // 0, 1 or 2

			// VFX
			VisualEffect vfx = new VisualEffect();

			Object target = getTarget(env, target1);
			if (target instanceof Person)
			{
				Person p = (Person) target;
				vfx.p1 = p.Point();
				vfx.z = p.z;
				// ALLY
				if (p.commanderID == user.commanderID)
				{
					vfx.subtype = 0;
					if (rand == 0) // Heal
						p.life = Math.min(p.maxLife, p.life + 3 * level); // 3 * Level HP
					if (rand == 1) // Remove effects
						p.removeEffects((int) (level + 1) / 2); // level/2 effects rounded up
					if (rand == 2) // Restore mana and stamina
					{
						p.mana = Math.min(p.maxMana, p.mana + level);
						p.stamina = Math.min(p.maxStamina, p.stamina + level);
					} // level mana and stamina
				}
				else // ENEMY
				{
					vfx.subtype = 1;
					if (rand == 0) // Damage
						env.hitPerson(p, 1 * level, 0, 0, -1); // 1 * Level damage
					if (rand == 1) // Nullify for Level seconds
						p.affect(new Nullified(level, true, this), true);
					if (rand == 2) // Damage random armor part
						p.damageArmorPart(p.armorParts[(int) (Math.random() * p.armorParts.length)], 10 * level, -1, 1);
				}
			}
			// BALL
			if (target instanceof Ball)
			{
				vfx.subtype = 2;
				Ball b = (Ball) target;
				vfx.p1 = b.Point();
				vfx.z = b.z;
				if (rand == 0) // Split
				{
					env.balls.add(new Ball(b.x + (int) ((b.radius + 3) * Math.cos(b.angle() + Math.PI / 2)), b.y + (int) ((b.radius + 3) * Math.sin(b.angle() + Math.PI / 2)), b.z, b.elementNum,
							b.damage, b.pushback, b.angle() + Math.PI / 4, user));
					env.balls.add(new Ball(b.x + (int) ((b.radius + 3) * Math.cos(b.angle() - Math.PI / 2)), b.y + (int) ((b.radius + 3) * Math.sin(b.angle() - Math.PI / 2)), b.z, b.elementNum,
							b.damage, b.pushback, b.angle() - Math.PI / 4, user));
					env.balls.remove(b);
				}
				if (rand == 1) // Make fast and randomize angle
				{
					double velocity = b.velocity() * Math.min(1.5, level / 2);
					double angle = Math.random() * 2 * Math.PI;
					b.xVel = velocity * Math.cos(angle);
					b.yVel = velocity * Math.sin(angle);
				}
				if (rand == 2) // turn into wall/pool/destroy
				{
					if (Ability.elementalAttacksPossible[b.elementNum][3]) // wall (metal, ice, flesh, earth, plant)
						env.addWall((int) (b.x / Environment.squareSize), (int) (b.y / Environment.squareSize), b.elementNum, true);
					else if (Ability.elementalAttacksPossible[b.elementNum][6]) // pool (water, acid, lava)
						env.addPool((int) (b.x / Environment.squareSize), (int) (b.y / Environment.squareSize), b.elementNum, true);
					else // destroy (fire, wind, electricity, energy)
						env.ballDebris(b, "shatter", Math.random() * Math.PI * 2);
					env.balls.remove(b);
				}
			}
			if (target instanceof Point)
			{
				vfx.subtype = 3;
				vfx.z = 0;
				Point p = (Point) target;
				vfx.p1 = new Point((int) ((p.x + 0.5) * Environment.squareSize), (int) ((p.y + 0.5) * Environment.squareSize));
				int minX = p.x - ((int) (level / 4));
				int minY = p.y - ((int) (level / 4));
				int maxX = p.x + ((int) (level / 4));
				int maxY = p.y + ((int) (level / 4));
				// WALL
				if (env.wallTypes[p.x][p.y] != -1)
				{
					for (int x = minX; x <= maxX; x++)
						for (int y = minY; y <= maxY; y++)
							if (env.wallTypes[x][y] != -1)
							{
								if (rand == 0) // Melt/Destroy
								{
									int meltElement = env.getWallElement(env.wallTypes[x][y]);
									int element = env.wallTypes[x][y];
									env.otherDebris(x, y, meltElement, "destroy", 0);
									if (Ability.elementalAttacksPossible[meltElement][6])
									{
										env.poolTypes[x][y] = meltElement;
										env.poolHealths[x][y] = 100;
										env.updatePoolCorners(x, y, meltElement);
									}
									env.wallTypes[x][y] = -1;
									env.wallHealths[x][y] = -1;
									env.updateWallCorners(x, y, element);
								}
								if (rand == 1) // Transform into Plant Pool
								{
									env.remove(x, y);
									env.addPool(x, y, 11, true); // Plant/vine Pool/pit
								}
								if (rand == 2) // Transform into Metal Wall
								{
									env.remove(x, y);
									env.addWall(x, y, 4, true); // Metal Wall
								}
							}
				}
				// POOL
				else if (env.poolTypes[p.x][p.y] != -1)
				{
					for (int x = minX; x <= maxX; x++)
						for (int y = minY; y <= maxY; y++)
							if (env.poolTypes[x][y] != -1)
							{
								if (rand == 0) // Destroy
								{
									int element = env.poolTypes[x][y];
									env.otherDebris(x, y, element, "destroy", 0);
									if (Ability.elementalAttacksPossible[element][3])
									{
										env.wallTypes[x][y] = element;
										env.wallHealths[x][y] = 100;
										env.updateWallCorners(x, y, element);
									}
									env.poolTypes[x][y] = -1;
									env.poolHealths[x][y] = -1;
									env.updatePoolCorners(x, y, element);
								}
								if (rand == 1) // Transform into Acid Pool
								{
									env.remove(x, y);
									env.addPool(x, y, 7, true); // Acid Pool
								}
								if (rand == 2) // Transform into Earth Wall
								{
									env.remove(x, y);
									env.addWall(x, y, 10, true); // Earth Wall
								}
							}
				}
				// FLOOR
				else
					for (int x = minX; x <= maxX; x++)
						for (int y = minY; y <= maxY; y++)
						{
							if (rand == 0) // Create ice wall
							{
								env.remove(x, y);
								env.addPool(x, y, 5, true); // Ice Wall
							}
							if (rand == 1) // Create lava pool
							{
								env.remove(x, y);
								env.addPool(x, y, 8, true); // Lava Pool
							}
							if (rand == 2) // Create flesh/blood pool or flesh wall
							{
								env.remove(x, y);
								if (Math.random() < 0.5)
									env.addWall(x, y, 9, true); // Flesh Wall
								else
									env.addPool(x, y, 9, true); // Flesh Pool
							}
						}
			}
			vfx.type = VisualEffect.Type.WILD_POWER;
			vfx.onTop = true;
			vfx.timeLeft = 1;
			vfx.duration = 1;
			env.visualEffects.add(vfx);
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.target = target;
		player.aimType = Player.AimType.WILD_POWER;
	}
}
