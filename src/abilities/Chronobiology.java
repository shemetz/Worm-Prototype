package abilities;

import java.awt.Point;

import effects.ChronobiologyEffect;
import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Chronobiology extends Ability
{

	double amount;
	boolean initialChange = false;
	ChronobiologyEffect effect;

	public Chronobiology(int p)
	{
		super("Chronobiology", p);
		cost = 3; // to switch
		costType = CostType.MANA;
		instant = true;

		amount = 1 + 0.2 * level;
		on = true; // on = fast, off = slow
	}

	public void use(Environment env, Person user, Point target)
	{
		if (user.mana >= cost)
		{
			user.mana -= cost;
			on = !on;
			effect.unapply(user);
			effect.strength = 1 / effect.strength;
			effect.apply(user);
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (!initialChange)
		{
			effect = new ChronobiologyEffect(-1, amount, this);
			user.affect(effect, true);
			initialChange = true;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
		{
			on = false;
			effect.unapply(user);
			effect.strength = 1 / effect.strength;
			effect.apply(user);
		}
		effect.unapply(user);
		initialChange = false;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
	}
}
