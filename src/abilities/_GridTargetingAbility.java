package abilities;

import java.awt.Point;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Person;
import mainClasses.Player;

public class _GridTargetingAbility extends Ability
{

	final int		squareSize	= 96;
	public int		targetGridX, targetGridY;
	public boolean	canBuildInTarget;
	public Area		rangeArea;

	public _GridTargetingAbility(String n, int p)
	{
		super(n, p);
		costType = CostType.MANA;
		rangeType = RangeType.CREATE_IN_GRID;
		maintainable = true;
		instant = true;

		targetGridX = -1;
		targetGridY = -1;
		canBuildInTarget = false;
		rangeArea = new Area();
	}

	public void updateStats()
	{
		cost = Math.max(3 - 0.3 * LEVEL, 0.8);
		cooldown = Math.max(3 - 0.3 * LEVEL, 0.3); // is used for creating the wall
		costPerSecond = 1;
		range = 600;
		
	}

	public void UPT(Environment env, Person player)
	{
		rangeArea = new Area();
		for (int i = (int) (player.x - range); i < (int) (player.x + range); i += squareSize)
			for (int j = (int) (player.y - range); j < (int) (player.y + range); j += squareSize)
				if (Math.pow(player.x - i / squareSize * squareSize - 0.5 * squareSize, 2) + Math.pow(player.y - j / squareSize * squareSize - 0.5 * squareSize, 2) <= range * range)
					rangeArea.add(new Area(new Rectangle2D.Double(i / squareSize * squareSize, j / squareSize * squareSize, squareSize, squareSize)));

		if (!player.maintaining) // keep them where they are even if cursor moves
		{
			targetGridX = player.target.x / squareSize;
			targetGridY = player.target.y / squareSize;
		}

		// stop creating thing if it collides with someone
		for (Person p : env.people)
		{
			for (int i = (int) (p.x - 0.5 * p.radius); i <= (p.x + 0.5 * p.radius); i += p.radius)
				for (int j = (int) (p.y - 0.5 * p.radius); j <= (p.y + 0.5 * p.radius); j += p.radius)
					rangeArea.subtract(new Area(new Rectangle2D.Double(i / squareSize * squareSize, j / squareSize * squareSize, squareSize, squareSize)));
		}
		canBuildInTarget = rangeArea.contains(player.target);
		player.target = new Point((int) targetGridX * squareSize + squareSize / 2, (int) targetGridY * squareSize + squareSize / 2);
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		MAIN.errorMessage("No no no, you need to EXTEND this class and OVERRIDE this method, goddammit");
	}
}
