package abilities;

import java.awt.Point;
import java.awt.geom.Point2D;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.SprayDrop;
import mainResourcesPackage.SoundEffect;

public class Spray_E extends Ability
{

	public Spray_E(String elementName, int p)
	{
		super("Spray <" + elementName + ">", p);
		costType = CostType.MANA;
		rangeType = RangeType.CONE;
		stopsMovement = false;
		maintainable = true;
		instant = true;

		sounds.add(new SoundEffect(elementName + " Beam.wav"));
	}

	public void updateStats()
	{
		costPerSecond = 2;
		cooldown = 1; // after running out of mana
		range = 500; // TODO make range depend on points
		arc = Math.PI * 1 / 2; // not really
		
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, user.target);
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		if (on) // deactivate
		{
			user.notAnimating = false;
			on = false;
			user.maintaining = false;
			if (user.mana <= 0.3)
				cooldownLeft = cooldown;
			stopAllSounds();
		}
		else if (!user.prone && !user.maintaining && cooldownLeft <= 0 && user.timeEffect != 0) // activate
		{
			user.notAnimating = true;
			user.maintaining = true;
			on = true;
			user.switchAnimation(2);
			sounds.get(0).loop();
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		double targetAngle = Math.atan2(target.y - user.y, target.x - user.x);
		user.rotate(targetAngle, deltaTime * user.timeEffect);

		setSounds(user.Point());
		double angle = user.rotation;
		arc = 10 * (1 - user.accuracy); // user accuracy is 0.85 for average person.
		final double sprayExitDistance = 50;
		if (cooldownLeft == 0)
			if (user.mana >= costPerSecond * deltaTime)
			{
				sounds.get(0).cont();
				double velocity = range * 1.4;
				double z = (user.z + 0.35) * (Math.random() * 0.95 + 0.05); // anywhere between 100% and 5% range
				double randomAngle = (Math.random() - 0.5) * arc + angle; // random angle within spray arc
				Point2D start = new Point2D.Double(user.x + sprayExitDistance * Math.cos(randomAngle), user.y + sprayExitDistance * Math.sin(randomAngle));
				SprayDrop sd = new SprayDrop(start.getX(), start.getY(), z, elementNum, level, randomAngle, velocity, user);
				sd.timeEffect = user.timeEffect;
				env.sprayDrops.add(sd);
				user.mana -= costPerSecond * deltaTime;
			}
			else
			{
				sounds.get(0).pause();
				cooldownLeft = cooldown;
			}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.aimType = Player.AimType.NONE;
		if (!player.leftMousePressed)
			player.rotate(angle, 3.0 * deltaTime);
	}
}
