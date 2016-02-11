package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;
import mainClasses.Resources;

public class Burning extends Effect
{
	public Burning(int strength1, Ability CA)
	{
		super("Burning", -1, strength1, CA);
		stackable = false;
		removeOnDeath = true;
	}
	
	public Effect clone()
	{
		Burning e = new Burning((int) this.strength, this.creatorAbility);
		e.timeLeft = this.timeLeft;
		e.strength = this.strength;
		e.animFrame = this.animFrame;
		e.stackable = this.stackable;
		e.removeOnDeath = this.removeOnDeath;
		return e;
	}

	public void apply(Person target)
	{
		target.panic = true;
	}

	public void unapply(Person target)
	{
		target.panic = false;
	}

	public void nextFrame(int frameNum)
	{
		if (frameNum % 17 == 0)
			animFrame++;
		if (animFrame >= Resources.effects.get(0).size())
			animFrame = 0;
	}
}
