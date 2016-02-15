package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Player;

public class Shield_E extends Ability
{

	public ArcForceField shield;

	public Shield_E(String elementName, int p)
	{
		super("Shield <" + elementName + ">", p);
		cost = 2;
		costPerSecond = 0.3;
		costType = CostType.MANA;
		cooldown = 5;
		range = -1;
		rangeType = RangeType.NONE;
		maintainable = true;

		shield = null;
	}

	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x);
		/*
		 * Create and hold an elemental shield (force field near you) that you can aim, and will protect you until it breaks.
		 */
		if (!user.maintaining && !user.prone)
		// activating the shield
		{
			if (cost / 5 > user.mana || cooldownLeft > 0)
				return;

			arc = Math.PI / 2; // arc of shield
			double minRadius = 80, maxRadius = 92;
			shield = new ArcForceField(user, angle, arc, minRadius, maxRadius, (int) (level * 10), this.getElementNum(), "Shield <" + this.getElement() + ">");
			boolean nope = false;
			for (Person p : env.people)
				if (env.personAFFCollision(p, shield))
				{
					nope = true;
				}
			if (!nope)
			{
				env.AFFs.add(shield);
				user.maintaining = true;
				user.rotation = angle;
				on = true;
				user.switchAnimation(2);
				user.notMoving = stopsMovement;
				user.notAnimating = true;
			}
			else
			{
				env.shieldDebris(shield, "deactivate");
				cooldownLeft = 0.25;
				shield = null;
			}
		}
		else if (on)
		// deactivating the shield
		{
			double remainingFFhealth = 0;
			for (int i = 0; i < env.AFFs.size(); i++)
				if (env.AFFs.get(i).equals(shield))
				{
					remainingFFhealth = env.AFFs.get(i).life + env.AFFs.get(i).extraLife;
					env.shieldDebris(env.AFFs.get(i), "deactivate");
					env.AFFs.remove(i);
					i--;
				}
			cooldownLeft = 1 + 0.8 * cooldown - 0.8 * cooldown * remainingFFhealth / (level * 10); // if shield had full HP, 1 cooldown. if had no HP, full
																									// cooldown.
			user.maintaining = false;
			on = false;
			user.notMoving = false;
			user.notAnimating = false;
		}
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, user.target);
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		if (user.mana < costPerSecond)
			use(env, user, target);
		else
		{
			double targetAngle = Math.atan2(target.y - user.y, target.x - user.x);
			user.rotate(targetAngle, deltaTime);

			user.mana -= costPerSecond * deltaTime;
			shield.rotation = user.rotation;
			if (shield.extraLife > 0)
				user.mana -= cost * deltaTime;
		}
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		double angle = Math.atan2(target.y - player.y, target.x - player.x);
		player.aimType = Player.AimType.NONE;
		if (!player.leftMousePressed) // stops aiming shield while pressing mouse, to blink for example
			player.rotate(angle, deltaTime);
	}
}
