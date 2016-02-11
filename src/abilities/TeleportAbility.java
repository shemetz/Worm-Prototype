package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class TeleportAbility extends Ability
{
	final int	squareSize	= 96;
	public double		triangle1;
	public double triangle2;
	public double triangle3;

	public TeleportAbility(String n, int p)
	{
		super(n, p);
		triangle1 = 0;
		triangle2 = 2;
		triangle3 = 4;
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
		player.aimType = Player.AimType.TELEPORT;
		player.successfulTarget = checkIfAvailable(target.x, target.y, player.z, env, player);

		// sweet awesome triangles
		triangle1 += 0.031;
		triangle2 += 0.053;
		triangle3 -= 0.041;
	}
}
