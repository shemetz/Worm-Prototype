package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Energy_Regeneration_II extends _PassiveAbility
{

	public Energy_Regeneration_II(int p)
	{
		super("Energy Regeneration II", p);
	}

	public void updateStats()
	{
		amount = 1 * LEVEL;
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.manaRegen += val * amount;
		user.staminaRegen += val * amount;
		on = !on;
	}
}
