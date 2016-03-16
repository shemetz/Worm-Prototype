package abilities;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import mainClasses.Ability;
import mainClasses.ArcForceField;
import mainClasses.Environment;
import mainClasses.Methods;
import mainClasses.Person;
import mainClasses.Player;
import mainResourcesPackage.SoundEffect;

public class Bubble_Target extends _AFFAbility
{
	public List<ArcForceField> bubbles;

	public Bubble_Target(int p)
	{
		super("Bubble Target", p);
		costType = CostType.MANA;
		rangeType = Ability.RangeType.CIRCLE_AREA;
		instant = false;

		bubbles = new ArrayList<ArcForceField>();

		sounds.add(new SoundEffect("Bubble_appear.wav"));
		sounds.add(new SoundEffect("Bubble_pop.wav"));
	}

	public void updateStats()
	{
		cooldown = Math.min(6 - level, 0.3);
		cost = 3;
		range = 500;

		life = 10 * level;
		armor = level * 2;
		decayRate = 0.1;
	}

	public void use(Environment env, Person user, Point target)
	{
		setSounds(user.Point());
		if (!user.maintaining && !user.prone) // activating bubble
		{
			if (cost > user.mana || cooldownLeft > 0)
				return;
			Person targetPerson = getTarget(env, user, target);
			if (targetPerson == null)
				return;

			// Add a new protective bubble
			ArcForceField bubble = new ArcForceField(targetPerson, 0, 2 * Math.PI, 107, life, 12, ArcForceField.Type.IMMOBILE_BUBBLE);
			bubble.armor = armor;
			env.AFFs.add(bubble);
			bubbles.add(bubble);
			user.mana -= this.cost;
			this.cooldownLeft = this.cooldown;
			this.on = true;
			sounds.get(0).play();
		}
	}

	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		for (int i = 0; i < bubbles.size(); i++)
		{
			ArcForceField bubble = bubbles.get(i);
			bubble.life -= bubble.life * decayRate * deltaTime;
			if (Methods.DistancePow2(bubble.target.Point(), bubble.Point()) > bubble.maxRadius * bubble.maxRadius) // if target got out
				bubble.life = 0;
			if (bubble.life <= 0)
			{
				bubbles.remove(i);
				i--;
			}
		}
		if (bubbles.isEmpty())
			on = false;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
	}

	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		player.target = target;
		player.aimType = Player.AimType.TARGET_IN_RANGE;
	}
}
