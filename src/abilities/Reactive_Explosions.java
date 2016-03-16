package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Reactive_Explosions extends _PassiveAbility
{
	Explosion_Resistance givenAbility;
	boolean haventGivenResistance;
	public double minimumDamageNeeded;
	public boolean prepareToExplode;

	public Reactive_Explosions(int p)
	{
		super("Reactive Explosions", p);
		rangeType = Ability.RangeType.EXPLOSION;

		haventGivenResistance = true;
		givenAbility = (Explosion_Resistance) Ability.ability("Explosion Resistance", 0);
		prepareToExplode = false;
	}

	public void updateStats()
	{
		cooldown = (double) 5 / LEVEL;
		radius = 400;
		damage = LEVEL * 1.5;
		pushback = LEVEL * 4;
		minimumDamageNeeded = Math.min(10 - LEVEL, 1);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = true;
		if (haventGivenResistance)
		{
			haventGivenResistance = false;
			user.abilities.add(givenAbility);
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		cooldownLeft -= deltaTime;
		if (cooldownLeft < 0)
			cooldownLeft = 0;
		if (prepareToExplode)
		{
			prepareToExplode = false;
			if (cooldownLeft == 0)
			{
				env.createExplosion(user.x, user.y, user.z, radius, damage, pushback, -1);
				cooldownLeft = cooldown;
			}
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		user.abilities.remove(givenAbility);
		on = false;
		haventGivenResistance = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.aimType = Player.AimType.NONE;
	}
}
