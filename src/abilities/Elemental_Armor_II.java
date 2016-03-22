package abilities;

public class Elemental_Armor_II extends _ArmorAbility
{

	public Elemental_Armor_II(String element, int p)
	{
		super("Elemental Armor II <"+element+">", p);
		armorType = element;
	}

	public void updateStats()
	{
		decayRate = 1.0 / 90; // 90 seconds
		cost = LEVEL;
		AR = 2 * LEVEL;
		touchDamage = 5;
	}
}
