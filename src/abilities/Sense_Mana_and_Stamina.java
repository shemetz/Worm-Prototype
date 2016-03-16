package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Sense_Mana_and_Stamina extends _PassiveAbility
{

	public Sense_Mana_and_Stamina(int p)
	{
		super("Sense Mana and Stamina", p);
		rangeType = RangeType.CIRCLE_AREA;
	}

	public void updateStats()
	{
		range = (int) (50 * Math.pow(2, LEVEL));
		
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
	}
}
