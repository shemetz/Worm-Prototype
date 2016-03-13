package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.NPC;
import mainClasses.Person;
import mainClasses.Player;

public class Danger_Sense extends _PassiveAbility
{

	public Danger_Sense(int p)
	{
		super("Danger Sense", p);
	}

	public void updateStats()
	{
		amount = Math.pow(0.9, level);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		if (user instanceof NPC)
		{
			if (on)
			{
				((NPC) user).instinctDelayTime *= 0.7 * level;
				((NPC) user).maximumDistanceICareAboutPow2 *= Math.pow(level, 3);
			}
			else
			{
				((NPC) user).instinctDelayTime /= 0.7 * level;
				((NPC) user).maximumDistanceICareAboutPow2 /= Math.pow(level, 3);
			}
		}
		if (user instanceof Player)
		{
			if (on)
				user.evasion = 1 - (1 - user.evasion) * amount;
			else
				user.evasion = 1 - (1 - user.evasion) / amount;
		}
	}
}
