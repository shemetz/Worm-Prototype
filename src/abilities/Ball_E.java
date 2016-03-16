package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Ball;
import mainClasses.Environment;
import mainClasses.Person;
import mainResourcesPackage.SoundEffect;

public class Ball_E extends _ProjectileAbility
{

	public Ball_E(String elementName, int p)
	{
		super("Ball <" + elementName + ">", p);
		costType = CostType.MANA;
		rangeType = RangeType.NONE;
		stopsMovement = false;
		maintainable = true;
		instant = true;

		for (int i = 1; i < 6; i++)
			sounds.add(new SoundEffect("Ball_" + i + ".wav"));
	}

	public void updateStats()
	{
		damage = 0.6 * LEVEL * Ability.elementalAttackNumbers[elementNum][0];
		pushback = 0.6 * LEVEL * Ability.elementalAttackNumbers[elementNum][1] + 1;
		cost = 5.0 / elementalAttackNumbers[elementNum][2];
		cooldown = 5.0 / elementalAttackNumbers[elementNum][2];

		startingDistance = 80;
		velocity = 500;
		size = 1;
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		if (on)
		{
			on = false;
			user.maintaining = false;
			user.notAnimating = false;
			user.abilityMaintaining = -1;
			return;
		}
		if (user.prone || user.maintaining)
			return;

		user.maintaining = true;
		on = true;
		user.switchAnimation(2);
		user.notAnimating = true;
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		setSounds(user.Point());
		double targetAngle = Math.atan2(target.y - user.y, target.x - user.x);
		user.rotate(targetAngle, deltaTime * user.timeEffect);

		if (cooldownLeft == 0)
			if (user.mana >= cost)
			{
				double angle = targetAngle + user.missAngle * (2 * Math.random() - 1);
				cooldownLeft = cooldown;
				Ball b = new Ball(elementNum, damage, pushback, angle, user, velocity);
				b.x = user.x + startingDistance * Math.cos(angle);
				b.y = user.y + startingDistance * Math.sin(angle);
				b.z = user.z + 0.9;
				b.xVel += user.xVel;
				b.yVel += user.yVel;
				b.size = size;
				b.timeEffect = user.timeEffect;

				// critical chance
				if (Math.random() < user.criticalChance)
					b.critical = true;
				env.balls.add(b);
				sounds.get((int) (Math.random() * 5)).play();
				user.mana -= cost;
				user.rotate(angle, deltaTime);
			}
	}
}
