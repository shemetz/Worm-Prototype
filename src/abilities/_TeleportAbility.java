package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;
import mainResourcesPackage.SoundEffect;

/**
 * An ability that teleports the user to a targeted location or direction.
 * 
 * @author Itamar
 *
 */
public class _TeleportAbility extends Ability
{
	final int squareSize = 96;
	public double triangle1;
	public double triangle2;
	public double triangle3;
	public boolean telefragging;

	public enum Type
	{
		FIXED_DISTANCE, TARGET_POINT
	};

	public Type type;

	public _TeleportAbility(String n, int p)
	{
		super(n, p);
		triangle1 = 0;
		triangle2 = 2;
		triangle3 = 4;
		telefragging = false;

		sounds.add(new SoundEffect("Blink_success.wav"));
		sounds.add(new SoundEffect("Blink_fail.wav"));
	}

	/**
	 * Teleports the user to the position of the target (or direction, if type is FIXED_DISTANCE). It is not possible to teleport into walls, and it is not possible to teleport into humans unless {@link #telefragging} is true.
	 */
	public void use(Environment env, Person user, Point target)
	{
		if (cooldownLeft > 0 || cost > user.mana)
			return;
		double originalX = user.x, originalY = user.y;

		double distance = Math.sqrt(Methods.DistancePow2(user.Point(), target)); // not important for FIXED_DISTANCE
		double trueCost = getCost(distance); // not important for FIXED_DISTANCE
		setSounds(user.Point());
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		if (type == Type.FIXED_DISTANCE)
		{
			// if not maintaining a power, the user will rotate to fit the teleportation.
			if (!user.maintaining)
				user.rotation = angle;

			user.x += range * Math.cos(angle);
			user.y += range * Math.sin(angle);
		}
		else if (type == Type.TARGET_POINT)
		{
			user.x = target.x;
			user.y = target.y;
		}
		if (checkIfAvailable(user.x, user.y, user.z, env, user)) // managed to teleport
		{
			user.mana -= trueCost;
			cooldownLeft = cooldown;
			final int numOfLines = 5;
			for (int j = 0; j < numOfLines; j++)
			{
				VisualEffect eff = new VisualEffect();
				eff.type = VisualEffect.Type.BLINK_SUCCESS;
				eff.duration = 0.4;
				eff.timeLeft = eff.duration;
				eff.p1p2variations = new Point(user.radius, user.radius);
				if (type == Type.FIXED_DISTANCE)
					eff.p1 = new Point((int) (user.x - range * Math.cos(angle)), (int) (user.y - range * Math.sin(angle)));
				else
					eff.p1 = new Point((int) (user.x - 300 * Math.cos(angle)), (int) (user.y - 300 * Math.sin(angle)));
				eff.p2 = new Point((int) (user.x), (int) (user.y));
				eff.onTop = false;
				env.visualEffects.add(eff);
				if (type == Type.TARGET_POINT)
				{
					VisualEffect eff2 = new VisualEffect();
					eff2.type = VisualEffect.Type.BLINK_SUCCESS;
					eff2.duration = 0.4;
					eff2.timeLeft = eff.duration;
					eff2.p1p2variations = new Point(user.radius, user.radius);
					eff.p1 = new Point((int) (originalX + 300 * Math.cos(angle)), (int) (originalY + 300 * Math.sin(angle)));
					eff.p2 = new Point((int) (originalX), (int) (originalY));
					eff.onTop = false;
					env.visualEffects.add(eff);
				}
			}
			// SFX
			sounds.get(0).play();

			// Telefrag
			for (Person p : env.people)
				if (!p.equals(user))
					if (p.z + p.height > user.z && p.z < user.z + user.height)
						if (Methods.DistancePow2(user.x, user.y, p.x, p.y) < Math.pow((user.radius + p.radius), 2))
							telefrag(env, p);
		}
		else
		// tried to blink into something
		{
			user.x = originalX;
			user.y = originalY;
			final int numOfLines = 3;
			for (int j = 0; j < numOfLines; j++)
			{
				VisualEffect eff = new VisualEffect();
				eff.type = VisualEffect.Type.BLINK_FAIL;
				eff.duration = 0.3;
				eff.timeLeft = eff.duration;
				eff.p1p2variations = new Point(user.radius, user.radius);
				if (type == Type.FIXED_DISTANCE)
					eff.p1 = new Point((int) (user.x + range * Math.cos(angle)), (int) (user.y + range * Math.sin(angle)));
				else
					eff.p1 = new Point((int) (user.x + 300 * Math.cos(angle)), (int) (user.y + 300 * Math.sin(angle)));
				eff.p2 = new Point((int) (user.x), (int) (user.y));
				eff.onTop = true;
				env.visualEffects.add(eff);
			}

			// SFX
			sounds.get(1).play();
		}
	}

	/**
	 * Should be overridden
	 * 
	 * @param distance
	 * @return cost to teleport that distance
	 */
	double getCost(double distance)
	{
		MAIN.errorMessage("No getCost method was created that overrides the real one! Ability name is " + name);
		return -1 * distance;
	}

	/**
	 * Deal damage to a victim. Damage is currently 15 points, and pushback is 10.
	 * 
	 * @param env
	 * @param victim
	 */
	void telefrag(Environment env, Person victim)
	{
		env.hitPerson(victim, 15, 10, Math.random() * Math.PI * 2, -1);
	}

	/**
	 * Says whether or not the targeted point is available for teleportation.
	 * 
	 * @param x
	 * @param y
	 * @param z
	 * @param env
	 * @param user
	 * @return if the spot is valid.
	 */
	public boolean checkIfAvailable(double x, double y, double z, Environment env, Person user)
	{
		// test walls
		if (!user.ghostMode && z < 1)
			for (int i = (int) (x - 0.5 * user.radius); i / squareSize <= (int) (x + 0.5 * user.radius) / squareSize; i += squareSize)
				for (int j = (int) (y - 0.5 * user.radius); j / squareSize <= (int) (y + 0.5 * user.radius) / squareSize; j += squareSize)
					if (i / squareSize >= 0 && j / squareSize >= 0 && i / squareSize < env.width && j / squareSize < env.height)
						if (env.wallTypes[i / squareSize][j / squareSize] != -1)
							return false;
		// test people
		if (!telefragging)
			for (Person p : env.people)
				if (!p.equals(user)) // pretty redundant
					if (p.z + p.height > user.z && p.z < user.z + user.height)
						if (Methods.DistancePow2(x, y, p.x, p.y) < Math.pow((user.radius + p.radius), 2))
							return false;
		return true;
	}

	/**
	 * Updates target for some cool animating triangles
	 */
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		if (type == Type.FIXED_DISTANCE)
		{
			double angle = Math.atan2(target.y - player.y, target.x - player.x);
			player.target = new Point((int) (player.x + range * Math.cos(angle)), (int) (player.y + range * Math.sin(angle)));
		}
		else if (type == Type.TARGET_POINT)
			player.target = target;

		target = player.target;
		player.aimType = Player.AimType.TELEPORT;
		player.successfulTarget = checkIfAvailable(target.x, target.y, player.z, env, player);

		// sweet awesome triangles
		triangle1 += 0.031;
		triangle2 += 0.053;
		triangle3 -= 0.041;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}
}
