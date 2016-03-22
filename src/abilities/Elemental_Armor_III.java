package abilities;

public class Elemental_Armor_III extends _ArmorAbility
{

	public Elemental_Armor_III(String element, int p)
	{
		super("Elemental Armor III <"+element+">", p);
		armorType = element;
	}

	public void updateStats()
	{
		decayRate = 1.0 / 300; // 5 minutes
		cost = LEVEL;
		AR = 4*LEVEL;
		touchDamage = 15;
	}
}
