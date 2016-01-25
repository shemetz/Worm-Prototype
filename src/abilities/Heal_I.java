package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainClasses.VisualEffect;
import effects.Heal;

public class Heal_I extends ApplyEffect
{

	public Heal_I(int p)
	{
		super("Heal I", p, new Heal("Heal", p), ApplyEffect.targetTypes.OTHER, 3);
		cost = 0;
		costPerSecond = 1;
		costType = "mana";
		cooldown = 0;
		range = 50 * points;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = true;
		instant = true;
	}

	public void use(Environment env, Person user, Point target)
	{
		/*
		 * Continuously heal the closest injured person within range, or yourself.
		 */
		if (on)
		{
			on = false;
			user.maintaining = false;
			user.abilityMaintaining = -1;
		} else if (!user.prone && !user.maintaining && cooldownLeft <= 0)
		{
			user.maintaining = true;
			on = true;
			targetEffect1 = 0;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.targetType = "";
		player.target = new Point(-1, -1);
	}
}
