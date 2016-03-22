package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Body_Regeneration extends _PassiveAbility
{

	public Body_Regeneration(int p)
	{
		super("Body Regeneration", p);
	}

	public void updateStats()
	{
		amount = 1.5 * LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.lifeRegen += 2 * val * amount;
		user.manaRegen += val * amount;
		user.staminaRegen += val * amount;
		on = !on;
	}
}
