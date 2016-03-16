package abilities;

import java.awt.Point;

import mainClasses.ArcForceField;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainResourcesPackage.SoundEffect;

public class Protective_Bubble_II extends _AFFAbility
{
	public ArcForceField bubble;

	public Protective_Bubble_II(int p)
	{
		super("Protective Bubble II", p);
		costType = CostType.MANA;
		instant = true;

		bubble = null;

		sounds.add(new SoundEffect("Bubble_appear.wav"));
		sounds.add(new SoundEffect("Bubble_pop.wav"));
	}

	public void updateStats()
	{
		cooldown = 1;
		cost = 4;
		life = 20 * level;
		armor = level;
		decayRate = 0.1;
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		// deactivating the bubble
		if (on && cooldownLeft == 0)
		{
			for (int i = 0; i < env.AFFs.size(); i++)
				if (env.AFFs.get(i).equals(bubble))
				{
					env.shieldDebris(bubble, "bubble");
					cooldownLeft = 0.5;
					on = false;
					env.AFFs.remove(i);
					i--;
					sounds.get(1).play();
				}
		}
		else if (!user.maintaining && !user.prone) // activating bubble
		{
			if (cost > user.mana || cooldownLeft > 0)
				return;

			// Remove any current protective bubble
			for (int i = 0; i < env.AFFs.size(); i++)
				if (env.AFFs.get(i).target.equals(user) && env.AFFs.get(i).type == ArcForceField.Type.MOBILE_BUBBLE)
				{
					env.shieldDebris(env.AFFs.get(i), "bubble");
					env.AFFs.remove(i);
					i--;
				}

			// Add a new protective bubble
			bubble = new ArcForceField(user, 0, 2 * Math.PI, 107, life, 12, ArcForceField.Type.MOBILE_BUBBLE);
			bubble.armor = armor;
			env.AFFs.add(bubble);
			user.mana -= this.cost;
			this.cooldownLeft = this.cooldown;
			this.on = true;
			sounds.get(0).play();
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		bubble.life -= bubble.life * decayRate * deltaTime;
		bubble.rotation = Methods.lerpAngle(bubble.rotation, user.rotation, deltaTime);
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		if (on)
			use(env, user, null);
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
	}
}
