package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Spray_E extends Ability
{

	public Spray_E(String elementName, int p)
	{
		super("Spray <"+elementName+">", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		
	}
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		
	}
}
