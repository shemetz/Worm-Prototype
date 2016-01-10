package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Shield_E extends Ability
{

	public Shield_E(String elementName, int p)
	{
		super("Shield <"+elementName+">", p);
	}
	
	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y-user.y, target.x-user.x);
		/*
		 * Create and hold an elemental shield (force field near you) that you can aim, and will protect you until it breaks.
		 */
		if (!user.maintaining && !user.prone)
		// activating the shield
		{
			if (cost / 5 > user.mana || cooldownLeft > 0)
				return;

			final double arc = Math.PI / 2, minRadius = 80, maxRadius = 92;
			env.arcFFs.add(new ArcForceField(user, angle, arc, minRadius, maxRadius, (int) (points * 10), getElementNum()));
			user.maintaining = true;
			on = true;
			user.switchAnimation(2);
			user.notMoving = stopsMovement;
			user.notAnimating = true;

		} else if (on)
		// deactivating the shield
		{
			double remainingFFhealth = 0;
			for (int i = 0; i < env.arcFFs.size(); i++)
				if (env.arcFFs.get(i).target.equals(user))
				{
					remainingFFhealth = env.arcFFs.get(i).life + env.arcFFs.get(i).extraLife;
					env.shieldDebris(env.arcFFs.get(i), "deactivate");
					env.arcFFs.remove(i);
					i--;
				}
			cooldownLeft = 1 + 0.8 * cooldown - 0.8 * cooldown * remainingFFhealth / (points * 10); // if shield had full HP, 1 cooldown. if had no HP, full
																																	// cooldown.
			user.maintaining = false;
			on = false;
			user.notMoving = false;
			user.notAnimating = false;
		}
	}
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y-player.y, target.x-player.x);
		player.targetType = "look";
		player.target = new Point(-1, -1);
		if (!player.leftMousePressed) // stops aiming shield while pressing mouse, to blink for example
			player.rotate(angle, deltaTime);
	}
}
