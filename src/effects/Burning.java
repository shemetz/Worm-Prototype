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
