package abilities;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class ForceFieldAbility extends Ability
{
	public int width, height;

	public ForceFieldAbility(String n, int p)
	{
		super(n, p);
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}
}
