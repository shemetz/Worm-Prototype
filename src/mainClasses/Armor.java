package mainClasses;

public class Armor extends Item
{
	double	maxArmorRating;
	double	armorRating; //Does not depend on the size of the armor - only "density of protection" or some shit like that
	String	name;
	boolean	isSkin; //Skin (=Natural Armor) does not get weaker by attacks that bypass it.

	public Armor(double AR, String n1)
	{
		maxArmorRating = AR;
		armorRating = AR;
		name = n1;
		isSkin = n1.contains("Skin");
	}

	public double effectiveness(int damageType)
	{
		switch (damageType)
		{
		case 1: // piercing
			return 0.5;
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
}
