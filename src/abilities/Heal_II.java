package abilities;

import java.awt.Point;

import effects.Healed;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.UIText;
import mainClasses.VisualEffect;

public class Heal_II extends ApplyEffect
{

	double healTextTimer;
	double lifeRegenBuff;

	public Heal_II(int p)
	{
		super("Heal II", p, ApplyEffect.targetTypes.AREA, VisualEffect.Type.HEAL);
		cost = 0;
		costPerSecond = 3;
		costType = "mana";
		cooldown = 0;
		range = 100 * level;
		rangeType = "Circle area";
		stopsMovement = false;
		maintainable = true;
		instant = true;

		healTextTimer = 0;
		lifeRegenBuff = 4 * level;
	}

	public boolean viableTarget(Person p, Person user)
	{
		if (!defaultViableTarget(p, user))
			return false;
		if (p.life / p.maxLife == 1)
			return false;
		if (p.commanderID != user.commanderID)
			return false;
		return true;
	}

	public Effect effect()
	{
		return new Healed(lifeRegenBuff, this);
	}

	public void maintain(Environment env, Person user, Point targetPoint, double deltaTime)
	{
		super.maintain(env, user, targetPoint, deltaTime);

		if (costPerSecond * deltaTime <= user.mana)
		{
			healTextTimer += deltaTime;
			if (healTextTimer > 1)
			{
				healTextTimer -= 1;
				for (Person p : targets)
					p.uitexts.add(new UIText(0, 0, "" + lifeRegenBuff, 2));
			}
		}
	}
}
