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

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		if (user instanceof NPC)
		{
			if (on)
				((NPC) user).instinctDelayTime *= 0.7 * level;
			else
				((NPC) user).instinctDelayTime /= 0.7 * level;
		}
		if (user instanceof Player)
		{
			if (on)
				user.evasion = 1 - (1 - user.evasion) * Math.pow(0.9, level);
			else
				user.evasion = 1 - (1 - user.evasion) / Math.pow(0.9, level);
		}
	}
}
