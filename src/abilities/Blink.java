package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;
import mainResourcesPackage.SoundEffect;

public class Blink extends Ability
{
	final int squareSize = 96;

	public Blink(int p)
	{
		super("Blink", p);
		cost = 1 + (double) (points) / 3;
		costType = "mana";
		cooldown = 0.1 + (double) (points) / 4;
		targetEffect1 = 0;
		targetEffect2 = 2;
		targetEffect3 = 4;
		range = points * 100;
		rangeType = "Exact range"; // maybe change it to up-to range?

		sounds.add(new SoundEffect("Blink_success.wav"));
		sounds.add(new SoundEffect("Blink_fail.wav"));
	}

	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		/*
		 * Teleport ahead to target direction
		 * 
		 * visual effect types:
		 * 
		 * 1 = just blinked, and there's an effect.
		 * 
		 * 2 = failed to blink
		 */
		if (cooldownLeft > 0 || cost > user.mana)
			return;

		// if not maintaining a power, the user will rotate to fit the teleportation.
		if (!user.maintaining)
			user.rotation = angle;

		user.x += range * Math.cos(angle);
		user.y += range * Math.sin(angle);
		if (checkIfAvailable(user.x, user.y, user.z, env, user)) // managed to teleport
		{
			user.mana -= cost;
			cooldownLeft = cooldown;
			final int numOfLines = 5;
			for (int j = 0; j < numOfLines; j++)
			{
				VisualEffect eff = new VisualEffect();
				eff.type = 1;
				eff.duration = 0.4;
				eff.timeLeft = eff.duration;
				eff.p1p2variations = new Point(user.radius, user.radius);
				eff.p1 = new Point((int) (user.x - range * Math.cos(angle)), (int) (user.y - range * Math.sin(angle)));
				eff.p2 = new Point((int) (user.x), (int) (user.y));
				eff.onTop = false;
				env.effects.add(eff);
			}
			// SFX
			sounds.get(0).play();
		} else
		// tried to blink into something
		{
			user.x -= range * Math.cos(angle);
			user.y -= range * Math.sin(angle);
			final int numOfLines = 3;
			for (int j = 0; j < numOfLines; j++)
			{
				VisualEffect eff = new VisualEffect();
				eff.type = 2;
				eff.duration = 0.3;
				eff.timeLeft = eff.duration;
				eff.p1p2variations = new Point(user.radius, user.radius);
				eff.p1 = new Point((int) (user.x + range * Math.cos(angle)), (int) (user.y + range * Math.sin(angle)));
				eff.p2 = new Point((int) (user.x), (int) (user.y));
				eff.onTop = true;
				env.effects.add(eff);
			}

			// SFX
			sounds.get(1).play();
		}
	}

	public boolean checkIfAvailable(double x, double y, double z, Environment env, Person user)
	{
		// test boundaries
		if (x < 0 || y < 0 || x > env.widthPixels || y > env.heightPixels)
			return false;
		// test walls
		if (!user.ghostMode && z < 1)
			for (int i = (int) (x - 0.5 * user.radius); i / squareSize <= (int) (x + 0.5 * user.radius) / squareSize; i += squareSize)
				for (int j = (int) (y - 0.5 * user.radius); j / squareSize <= (int) (y + 0.5 * user.radius) / squareSize; j += squareSize)
					if (env.wallTypes[i / squareSize][j / squareSize] != -1)
						return false;
		// test people
		for (Person p : env.people)
			if (!p.equals(user)) // pretty redundant
				if (p.z + p.height > user.z && p.z < user.z + user.height)
					if (Methods.DistancePow2(x, y, p.x, p.y) < Math.pow((user.radius + p.radius), 2))
						return false;
		return true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		target = player.target;
		player.targetType = "teleport";
		player.successfulTarget = checkIfAvailable(target.x, target.y, player.z, env, player);

		// sweet awesome triangles
		targetEffect1 += 0.031;
		targetEffect2 += 0.053;
		targetEffect3 -= 0.041;
	}
}
