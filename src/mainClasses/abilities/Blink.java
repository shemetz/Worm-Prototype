package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;

public class Blink extends Ability
{
	final int squareSize = 96;

	public Blink(int p)
	{
		super("Blink", p);
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
		boolean cannot = false;
		// if not maintaining a power, the user will rotate to fit the teleportation.
		if (!user.maintaining)
			user.rotation = angle;
		user.x += range * Math.cos(angle);
		user.y += range * Math.sin(angle);
		// test boundaries
		if (user.x < 0 || user.y < 0 || user.x > env.widthPixels || user.y > env.heightPixels)
		{
			cannot = true;
			user.x -= range * Math.cos(angle);
			user.y -= range * Math.sin(angle);
		}
		// test collisions (with walls only! TEMP TODO)
		if (!cannot && !user.ghostMode)
			for (int i = (int) (user.x - 0.5 * user.radius); !cannot && i / squareSize <= (int) (user.x + 0.5 * user.radius) / squareSize; i += squareSize)
				for (int j = (int) (user.y - 0.5 * user.radius); !cannot && j / squareSize <= (int) (user.y + 0.5 * user.radius) / squareSize; j += squareSize)
					if (env.wallTypes[i / squareSize][j / squareSize] != -1)
					{
						cannot = true;
						user.x -= range * Math.cos(angle);
						user.y -= range * Math.sin(angle);
					}
		if (!cannot) // managed to teleport
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
			playSound("Blink success");
		} else
		// tried to blink into something
		{
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
			playSound("Blink fail");
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		player.targetType = "teleport";
		player.successfulTarget = true;
		// test boundaries
		if (player.target.x < 0 || player.target.y < 0 || player.target.x > env.widthPixels || player.target.y > env.heightPixels)
			player.successfulTarget = false;
		else if (!player.ghostMode)
			for (int i = (int) (player.target.x - 0.5 * player.radius); player.successfulTarget && i / squareSize <= (int) (player.target.x + 0.5 * player.radius) / squareSize; i += squareSize)
				for (int j = (int) (player.target.y - 0.5 * player.radius); player.successfulTarget && j / squareSize <= (int) (player.target.y + 0.5 * player.radius) / squareSize; j += squareSize)
					if (env.wallTypes[i / squareSize][j / squareSize] != -1)
						player.successfulTarget = false;

		// sweet awesome triangles
		targetEffect1 += 0.031;
		targetEffect2 += 0.053;
		targetEffect3 -= 0.041;
	}
}
