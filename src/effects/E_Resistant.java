package effects;

import mainClasses.Ability;
import mainClasses.Effect;

public class E_Resistant extends Effect
{
	public E_Resistant(String element, int strength1, Ability CA)
	{
		super(element + " Resistant", -1, strength1, CA);
	}

	public void init()
	{
		stackable = true;
		removeOnDeath = false;
	}

	public void nextFrame(int frameNum)
	{

	}
}
