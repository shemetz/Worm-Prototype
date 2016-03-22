package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mainClasses.Ability;
import mainClasses.EP;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Sensibility extends _PassiveAbility
{

	double timer;
	List<Ability> givenAbilities;

	static String[] possibilities = new String[]
	{ "Sense Parahumans", "Sense Life", "Sense Mana and Stamina", "Danger Sense", "Sense Powers", "Sense Element", "Sense Structure" };

	public Sensibility(int p)
	{
		super("Sensibility", p);
	}

	public void updateStats()
	{
		;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = true;
		givenAbilities = new ArrayList<Ability>();
		givenAbilities.add(randomAbility());
		givenAbilities.add(randomAbility());
		user.abilities.addAll(givenAbilities);
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		timer += deltaTime;

		if (timer > 60 * 5) // every 5 minutes
		{
			for (Ability a : givenAbilities)
				a.disable(env, user);
			user.abilities.removeAll(givenAbilities);
			givenAbilities.clear();
			givenAbilities.add(randomAbility());
			givenAbilities.add(randomAbility());
			user.abilities.addAll(givenAbilities);
			timer = 0;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
		{
			for (Ability a : givenAbilities)
				a.disable(env, user);
			user.abilities.removeAll(givenAbilities);
			givenAbilities.clear();
			on = false;
		}
	}

	Ability randomAbility()
	{
		String str = possibilities[(int) (Math.random() * possibilities.length)];
		if ("Sense Element".equals(str))
			str = str + " <" + EP.elementList[(int) (Math.random() * 12)] + ">";
		return Ability.ability(str, LEVEL);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		;
	}
}
