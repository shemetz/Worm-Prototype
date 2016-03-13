package abilities;

import java.awt.Point;

import effects.E_Resistant;
import mainClasses.EP;
import mainClasses.Environment;
import mainClasses.Person;

public class Elemental_Resistance_E extends _PassiveAbility
{
	public static String[] applicable = new String[]
	{ "Fire", "Lava", "Acid", "Electricity", "Energy" };
	int damageType;

	public Elemental_Resistance_E(String element, int p)
	{
		super("Elemental Resistance <" + element + ">", p);
		elementNum = EP.toInt(element);

		damageType = EP.damageType(element);
	}

	public void updateStats()
	{
		amount = level;
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		user.affect(new E_Resistant(EP.elementList[elementNum], (int) amount, this), on);
	}
}
