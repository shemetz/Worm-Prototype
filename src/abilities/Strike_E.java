package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Strike_E extends Ability
{

	public Strike_E(String elementName, int p)
	{
		super("Strike <" + elementName + ">", p);
	}

	public void use(Environment env, Person user, Point target)
	{

	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		// TODO update this part
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{

	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{

	}
}
