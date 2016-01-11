package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.ForceField;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Ball_E extends Ability
{

	public Ball_E(String elementName, int p)
	{
		super("Ball <"+elementName+">", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
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
		if (cooldownLeft == 0)
			if (user.mana >= cost)
			{
				double angle = Math.atan2(target.y - user.y, target.x - user.x);
				angle = angle + user.missAngle * (2 * Math.random() - 1);
				cooldownLeft = cooldown;
				mainClasses.Ball b = new mainClasses.Ball(getElementNum(), points, angle);
				b.x = user.x + range * Math.cos(angle);
				b.y = user.y + range * Math.sin(angle);
				b.z = user.z + 0.9;
				b.xVel += user.xVel;
				b.yVel += user.yVel;

				// TODO move this to a new method in Environment?
				boolean ballCreationSuccess = true;

				Point ballCenter = new Point((int) b.x, (int) b.y);
				// pow2 to avoid using Math.sqrt(), which is supposedly computationally expensive.
				double ballRadiusPow2 = Math.pow(b.radius, 2);
				// test force field collision
				for (ForceField ff : env.FFs)
					if (ff.x - 0.5 * ff.length <= b.x + b.radius && ff.x + 0.5 * ff.length >= b.x - b.radius && ff.y - 0.5 * ff.length <= b.y + b.radius
							&& ff.y + 0.5 * ff.length >= b.y - b.radius)
						if ((0 <= Methods.realDotProduct(ff.p[0], ballCenter, ff.p[1]) && Methods.realDotProduct(ff.p[0], ballCenter, ff.p[1]) <= ff.width * ff.width
								&& 0 <= Methods.realDotProduct(ff.p[0], ballCenter, ff.p[3]) && Methods.realDotProduct(ff.p[0], ballCenter, ff.p[3]) <= ff.length * ff.length)
								|| Methods.LineToPointDistancePow2(ff.p[0], ff.p[1], ballCenter) < ballRadiusPow2 || Methods.LineToPointDistancePow2(ff.p[2], ff.p[3], ballCenter) < ballRadiusPow2
								|| Methods.LineToPointDistancePow2(ff.p[1], ff.p[2], ballCenter) < ballRadiusPow2 || Methods.LineToPointDistancePow2(ff.p[3], ff.p[0], ballCenter) < ballRadiusPow2)
						{
							ballCreationSuccess = false;
							// damage FF
							double damage = (b.getDamage() + b.getPushback()) * 0.5; // half damage, because the ball "bounces"
							env.damageFF(ff, damage, ballCenter);
						}
				if (ballCreationSuccess)
					env.balls.add(b);
				else
					env.ballDebris(b, "shatter", b.angle());
				user.mana -= cost;
				user.rotate(angle, deltaTime);
			}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.targetType = "look";
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
