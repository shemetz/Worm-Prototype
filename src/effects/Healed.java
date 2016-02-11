package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Healed extends Effect
{
	public Healed(double lifeRegenBuff, Ability creatorAbility1)
	{
		super("Healed", -1, lifeRegenBuff, creatorAbility1);
		stackable = true;
		removeOnDeath = true;
	}

	public Effect clone()
	{
		Healed e = new Healed(this.strength, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.removeOnDeath = this.removeOnDeath;
		return e;
	}

	public void apply(Person target)
	{
		target.lifeRegen += 2 * strength;
	}

	public void unapply(Person target)
	{
		target.lifeRegen -= 2 * strength;
	}

	public void nextFrame(int frameNum)
	{

	}
}
