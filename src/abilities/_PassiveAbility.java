package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.MAIN;
import mainClasses.Person;
import mainClasses.Player;

/**
 * A passive ability. Activated once (in {@link Person#selfFrame(double)}, and modifies that person (e.g. increasing STRENGTH) or waits until a method checks if it exists (e.g. Reactive Explosions).
 * 
 * @author Itamar
 *
 */
public class _PassiveAbility extends Ability
{

	public _PassiveAbility(String s, int p)
	{
		super(s, p);
	}

	/**
	 * Should be overridden.
	 */
	public void use(Environment env, Person user, Point target)
	{
		// int val = on ? -1 : 1;
		// user.variable += something*val;
		// on = !on;
		MAIN.errorMessage("You should write your own thing in here, dumbass");
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		;
	}

	/**
	 * Activates the ability again, turning it off.
	 */
	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, null);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		;
	}
}
