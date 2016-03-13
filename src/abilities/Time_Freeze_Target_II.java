package abilities;

import java.awt.Point;

import effects.Time_Stopped;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Time_Freeze_Target_II extends Ability
{

	public Time_Freeze_Target_II(int p)
	{
		super("Time Freeze Target II", p);
		rangeType = RangeType.CIRCLE_AREA;
		costType = CostType.MANA;
	}

	public void updateStats()
	{
		cost = 2;
		cooldown = 0.2;
		range = 1500;
		duration = 2 * level;

	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void use(Environment env, Person user, Point target1)
	{
		Person target = getTarget(env, user, target1);
		if (target == null)
			return;
		if (user.mana >= cost && cooldownLeft <= 0)
		{
			user.mana -= cost;
			cooldownLeft = cooldown;
			Time_Stopped effect = new Time_Stopped(duration, this);
			target.affect(effect, true);
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.target = target;
		player.aimType = Player.AimType.TARGET_IN_RANGE;
	}
}
