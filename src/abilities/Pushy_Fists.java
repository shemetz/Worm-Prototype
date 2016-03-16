package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;
import mainResourcesPackage.SoundEffect;

public class Pushy_Fists extends _PunchAbility
{
	double strengthBonus;
	double pushbackResistanceBonus;

	public Pushy_Fists(int p)
	{
		super("Pushy Fists", p);

		sounds.add(new SoundEffect("Pushy Fists.wav"));
	}

	public void updateStats()
	{
		pushback = 4 * LEVEL;
		strengthBonus = 0.5 * LEVEL;
		pushbackResistanceBonus = 0.5;
		
	}

	@Override
	public void use(Environment env, Person user, Point target)
	{
		super.use(env, user, target);
		user.STRENGTH += strengthBonus * (on ? 1 : -1);
		user.pushbackResistance = 1 - (1 - user.pushbackResistance) * (on ? pushbackResistanceBonus : 1 / pushbackResistanceBonus);
	}

}
