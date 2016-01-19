package abilities;

import java.awt.Point;

import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainResourcesPackage.SoundEffect;

public class Protective_Bubble_I extends Ability
{
	public ArcForceField bubble;

	public Protective_Bubble_I(int p)
	{
		super("Protective Bubble I", p);
		cooldown = 1;
		costType = "mana";
		cost = 3;
		instant = true;

		bubble = null;

		sounds.add(new SoundEffect("Protective Bubble_appear.wav"));
		sounds.add(new SoundEffect("Protective Bubble_pop.wav"));
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		double angle = Math.atan2(target.y - user.y, target.x - user.x); // can be 0, honestly
		// deactivating the bubble
		if (on && cooldownLeft == 0)
		{
			for (int i = 0; i < env.arcFFs.size(); i++)
				if (env.arcFFs.get(i).equals(bubble))
				{
					env.shieldDebris(bubble, "bubble");
					cooldownLeft = 0.5;
					on = false;
					env.arcFFs.remove(i);
					i--;
					sounds.get(1).play();
				}
		} else if (!user.maintaining && !user.prone) // activating bubble
		{
			if (cost > user.mana || cooldownLeft > 0)
				return;

			// Remove any current protective bubble
			for (int i = 0; i < env.arcFFs.size(); i++)
				if (env.arcFFs.get(i).target.equals(user) && env.arcFFs.get(i).type.equals("Protective Bubble"))
				{
					env.shieldDebris(env.arcFFs.get(i), "bubble");
					env.arcFFs.remove(i);
					i--;
				}
			double AFFwidth = 5;// 5 + 5*points; //should be
			bubble = new ArcForceField(user, angle, 2 * Math.PI, 100, 100 + AFFwidth, 10 * points, 12, "Protective Bubble");
			env.arcFFs.add(bubble);
			user.mana -= this.cost;
			this.cooldownLeft = this.cooldown;
			this.on = true;
			sounds.get(0).play();
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		bubble.life -= bubble.life * 0.1 * deltaTime;
		bubble.rotation = Methods.lerpAngle(bubble.rotation, user.rotation, deltaTime);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
	}
}
