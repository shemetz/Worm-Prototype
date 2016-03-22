package abilities;

public class Elemental_Armor_I extends _ArmorAbility
{

	public Elemental_Armor_I(String element, int p)
	{
		super("Elemental Armor I <"+element+">", p);
		armorType = element;
	}

	public void updateStats()
	{
		decayRate = 1.0 / 30; // 30 seconds
		cost = LEVEL;
		AR = LEVEL;
		touchDamage = 0;
	}
}
