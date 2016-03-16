package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Wound_Regeneration_II extends _PassiveAbility
{

	public Wound_Regeneration_II(int p)
	{
		super("Wound Regeneration II", p);
	}
	
	public void updateStats()
	{
		amount = 2 * LEVEL;
		
	}

	public void use(Environment env, Person user, Point target)
	{
		int val = on ? -1 : 1;
		user.lifeRegen += val * amount;
		on = !on;
	}
}
