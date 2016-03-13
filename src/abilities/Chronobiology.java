package abilities;

import java.awt.Point;

import effects.ChronobiologyEffect;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Chronobiology extends Ability
{

	boolean initialChange = false;
	ChronobiologyEffect effect;
	public boolean state; // true = fast, false = slow

	public Chronobiology(int p)
	{
		super("Chronobiology", p);
		costType = CostType.MANA;
		instant = true;

		state = true;
		on = true; // stays like that
	}

	public void updateStats()
	{
		amount = 1 + 0.2 * level;
		cost = 3; // to switch
	}

	public void use(Environment env, Person user, Point target)
	{
		if (user.mana >= cost)
		{
			user.mana -= cost;
			state = !state;
			if (!disabled)
				effect.unapply(user);
			effect.strength = 1 / effect.strength;
			if (!disabled)
				effect.apply(user);
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (!initialChange)
		{
			on = true;
			effect = new ChronobiologyEffect(-1, amount, this);
			if (!state)
				effect.strength = 1 / effect.strength;
			user.affect(effect, true);
			initialChange = true;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		user.affect(effect, false);
		initialChange = false;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
	}
}
