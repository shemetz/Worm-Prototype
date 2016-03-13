package abilities;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import mainClasses.Ability;
import mainClasses.Environment;
import mainClasses.Person;

public class Elemental_Combat_I_E extends Ability
{
	public Elemental_Combat_I_E(String elementName, int p)
	{
		super("Elemental Combat I <" + elementName + ">", p);
	}

	public void updateStats()
	{
		; // TODO maybe
	}

	public void use(Environment env, Person user, Point target)
	{
		List<Ability> addedAbilities = new LinkedList<Ability>();
		// TODO make variances in the abilities - but make sure that with those variances the ability can still be turned on and off and they'll stay constant. MAke them dependent on some seed of this ability?
		for (int i = 0; i < 3; i++)
			if (elementalAttacksPossible[elementNum][i])
				addedAbilities.add(Ability.ability((elementalAttacks[i]) + " <" + getElement() + ">", level));

		if (!on)
			user.abilities.addAll(addedAbilities);
		else
			user.abilities.removeAll(addedAbilities);
		on = !on;
	}

	public void disable(Environment env, Person user)
	{
		disabled = true;
		//DOES NOT REMOVE THE ADDED ABILITIES
	}
}
