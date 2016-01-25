package effects;

import mainClasses.Effect;

public class E_Resistant extends Effect
{
	public E_Resistant(String element, int strength1)
	{
		super(element+" Resistant", -1, strength1);
		stackable = true;
	}
}
