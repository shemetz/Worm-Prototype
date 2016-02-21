package effects;

import mainClasses.Ability;
import mainClasses.Effect;
import mainClasses.Person;

public class Twitching extends Effect
{
	public Twitching(Ability creatorAbility1)
	{
		super("Twitch", 0.5, -1, creatorAbility1);
		stackable = false;
		removeOnDeath = true;
	}

	public Effect clone()
	{
		Twitching e = new Twitching(this.creatorAbility);
		e.timeLeft = this.timeLeft;
		return e;
	}

	public void apply(Person target)
	{
		target.twitching = true;
	}

	public void unapply(Person target)
	{
		target.twitching = false;
		target.abilityTryingToRepetitivelyUse = -1;
	}

	public void nextFrame(int frameNum)
	{

	}
}
