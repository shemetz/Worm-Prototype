package abilities;

import java.awt.Point;

import effects.Healed;
import mainClasses.Effect;
import mainClasses.Environment;
import mainClasses.Person;
import mainClasses.Resources;
import mainClasses.UIText;
import mainClasses.VisualEffect;

public class Heal_I extends _ApplyEffect
{

	double healTextTimer;
	double lifeRegenBuff;

	public Heal_I(int p)
	{
		super("Heal I", p, _ApplyEffect.targetTypes.OTHER, VisualEffect.Type.CONNECTING_BEAM);
		costType = CostType.MANA;
		rangeType = RangeType.CIRCLE_AREA;
		stopsMovement = false;
		maintainable = true;
		instant = true;

		healTextTimer = 0;
		beamImage = Resources.abilities.get("heal");
	}

	public void updateStats()
	{
		costPerSecond = 1;
		range = 50 * level;
		lifeRegenBuff = 2 * level;
		
	}

	public boolean viableTarget(Person p, Person user)
	{
		if (!super.viableTarget(p, user))
			return false;
		if (p.life == p.maxLife)
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
					p.uitexts.add(new UIText(0, 0, "" + (int) lifeRegenBuff, 2));
			}
		}
	}
}
