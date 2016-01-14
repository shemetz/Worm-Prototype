package mainClasses.abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;

public class Protective_Bubble_I extends Ability
{
	public ArcForceField bubble;

	public Protective_Bubble_I(int p)
	{
		super("Protective Bubble I", p);
		cooldown = Math.min(6 - points, 0.3);
		costType = "mana";
		cost = 3;

		bubble = null;
	}

	public void use(Environment env, Person user, Point target)
	{
		double angle = Math.atan2(target.y - user.y, target.x - user.x); // can be 0, honestly
		// activating the bubble
		if (!this.on)
		{
			if (!user.maintaining && !user.prone)
			{
				if (cost > user.mana || cooldownLeft > 0)
					return;
				double AFFwidth = 5;// 5 + 5*points; //should be
				bubble = new ArcForceField(user, angle, 2 * Math.PI, 100, 100 + AFFwidth, 10 * points, 12);
				env.arcFFs.add(bubble);
				user.mana -= this.cost;
				this.cooldownLeft = this.cooldown;
				this.on = true;
				// TODO sound effects
			}
		} else // deactivate
		{
			this.on = false;
			this.cooldownLeft = 0.5 * this.cooldown;
			bubble.life = 0; // kill
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		bubble.life -= bubble.life * 0.1 * deltaTime;
		bubble.rotation = Methods.lerpAngle(bubble.rotation,user.rotation, deltaTime);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
	}
}
