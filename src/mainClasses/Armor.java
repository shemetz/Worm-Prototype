package mainClasses;

public class Armor extends Item
{
	public double maxArmorRating;
	public double armorRating; // Does not depend on the size of the armor - only "density of protection" or some shit like that
	public String name;
	public boolean isSkin; // Skin (=Natural Armor) does not get weaker by attacks that bypass it.
	public boolean equipped;

	public Armor(double AR, String n1)
	{
		maxArmorRating = AR;
		armorRating = AR;
		name = n1;
		isSkin = n1.contains("Skin");
		equipped = false;
	}

	public double effectiveness(int damageType)
	{
		switch (damageType)
		{
		case 0: //blunt
			return 1;
		case 1: // piercing
			return 0.5;
		case 2: // burn
			if (name.equals("Plant"))
				return -1;
			if (name.equals("Lava") || name.equals("Fire"))
				return 2;
			return 1;
		case 3: // acid
			if (name.equals("Plant") || name.equals("Flesh"))
				return -1;
			if (name.equals("Acid"))
				return 2;
			return 1;
		case 4: // shock
			if (name.equals("Energy") || name.equals("Electricity"))
				return 2;
			return 1;
		case -1: // spectral? ethereal? phantom?
		case 9: // spectral? ethereal? phantom?
			return 0;
		default:
			return 1;
		}
	}

	public void reduce(double amount)
	{
		if (!isSkin)
			armorRating -= amount;
	}

	public boolean isElemental()
	{
		switch (name)
		{
		case "Fire":
		case "Water":
		case "Wind":
		case "Electricity":
		case "Metal":
		case "Energy":
		case "Ice":
		case "Acid":
		case "Lava":
		case "Flesh":
		case "Earth":
		case "Plant":
			return true;
		default:
			return false;
		}
	}
}
