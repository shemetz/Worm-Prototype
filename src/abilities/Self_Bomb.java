package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Self_Bomb extends Ability
{
	Explosion_Resistance givenAbility;
	boolean haventGivenResistance;

	public Self_Bomb(int p)
	{
		super("Self-Bomb", p);
		cost = 3;
		costType = CostType.MANA;
		cooldown = 1;
		radius = level * 400;
		damage = level * 4;
		pushback = level * 10;

		haventGivenResistance = true;
		givenAbility = (Explosion_Resistance) Ability.ability("Explosion Resistance", 0);
	}

	public void use(Environment env, Person user, Point target)
	{
		if (haventGivenResistance)
		{
			// TODO have this in an "initializeAbility" method of some sort?
			haventGivenResistance = false;
			user.abilities.add(givenAbility);
		}
		if (user.mana >= cost && !user.maintaining && cooldownLeft == 0)
		{
			// TODO make it not only in the user'z Z but in the one the user tried to do (most likely 0, unless cursor is above another object)
			env.createExplosion(user.x, user.y, user.z, radius, damage, pushback, -1);
			user.mana -= cost;
			cooldownLeft = cooldown;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		user.abilities.remove(givenAbility);
		haventGivenResistance = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.AIMLESS;
	}
}
