package mainClasses;

public class Clone extends NPC
{
	public Person master;
	public Ability creatorAbility;
	public double timeLeft; // -1 = disappears at end of combat

	public Clone(double x1, double y1, Person p, Ability creatorAbility1)
	{
		super(x1, y1, Strategy.AGGRESSIVE); // TODO maybe not aggressive?
		master = p;
		creatorAbility = creatorAbility1;
		legs = p.legs;
		chest = p.chest;
		head = p.head;
		hair = p.hair;
		initAnimation();
		commanderID = p.commanderID;
		inCombat = true;
		timeLeft = 0.2; // default but should be changed wherever this is created of course
		name = master.name + "'s Clone";
	}

	@Override
	public void die()
	{
		super.die();
		// TODO special effects

		// start cooldown on ability that summoned this
		for (Ability a : master.abilities)
			if (a.equals(creatorAbility))
			{
				a.on = false;
				a.cooldownLeft = a.cooldown;
			}
	}
}
