package abilities;

import java.awt.Point;

import effects.Time_Stopped;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Time_Freeze_Target_I extends Ability
{

	public Time_Freeze_Target_I(int p)
	{
		super("Time Freeze Target I", p);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		cost = 5;
		cooldown = 3 * LEVEL;
		range = 500;
		duration = 2 * LEVEL;

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
