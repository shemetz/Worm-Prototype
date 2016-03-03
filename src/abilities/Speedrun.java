package abilities;

import java.awt.Point;

import mainClasses.Environment;
import mainClasses.Person;

public class Speedrun extends _PassiveAbility
{

	public Speedrun(int p)
	{
		super("Speedrun", p);
	}

	public void use(Environment env, Person user, Point target)
	{
		on = !on;
		int val = on ? 1 : -1;
		user.runSpeed += val * 150 * level;
		user.runAccel += val * 1000 * level;
	}
}
