package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Ball;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;
import mainResourcesPackage.SoundEffect;

public class Ball_E extends Ability
{

	public Ball_E(String elementName, int p)
	{
		super("Ball <" + elementName + ">", p);
		cost = 5.0 / elementalAttackNumbers[elementNum][2];
		costType = CostType.MANA;
		cooldown = 5.0 / elementalAttackNumbers[elementNum][2];
		range = 80;
		rangeType = RangeType.NONE;
		stopsMovement = false;
		maintainable = true;
		instant = true;

		damage = 0.6 * level * Ability.elementalAttackNumbers[elementNum][0];
		pushback = 0.6 * level * Ability.elementalAttackNumbers[elementNum][1] + 1;

		for (int i = 1; i < 6; i++)
			sounds.add(new SoundEffect("Ball_" + i + ".wav"));
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
				Ball b = new Ball(elementNum, damage, pushback, angle, user);
				b.x = user.x + range * Math.cos(angle);
				b.y = user.y + range * Math.sin(angle);
				b.z = user.z + 0.9;
				b.xVel += user.xVel;
				b.yVel += user.yVel;
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

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.aimType = Player.AimType.NONE;
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
