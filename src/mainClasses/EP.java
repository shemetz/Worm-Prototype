package mainClasses;
public class EP
{
	public int elementNum;
	public int points;

	public EP(int e, int p)
	{
		elementNum = e;
		points = p;
	}

	public EP(String e, int p)
	{
		elementNum = toInt(e);
		points = p;
	}

	public String toString2()
	{ // This other version of toString is in uppercase if it's a main element, and lowercase if it's minor. Easier to read that way, for me.
		if (points < 4)
			return "" + elementList[elementNum].toLowerCase() + " " + points;
		return "" + elementList[elementNum].toUpperCase() + " " + points;
	}

	public String toString()
	{
		return "" + elementList[elementNum] + " " + points;
	}

	public static int damageType(String element)
	{
		switch (element)
		{
		case "Water":
		case "Wind":
		case "Metal":
		case "Earth":
		case "Plant":
			return 0; // impact
		case "Ice":
		case "Flesh":
			return 1; // stab
		case "Fire":
		case "Lava":
			return 2; // fire
		case "Acid":
			return 3; // acid
		case "Electricity":
		case "Energy":
		case "Force Field":
			return 4; // shock
		default:
			MAIN.errorMessage("5555: Unknown element! " + element);
			return -1;
		}
	}

	public static int damageType(int eNum)
	{
		switch (eNum)
		{
		case -2: //outer bound walls
			return -2;
		case 1:
		case 2:
		case 4:
		case 10:
		case 11:
			return 0; //blunt
		case 5:
		case 9:
			return 1;
		case 0:
		case 8:
			return 2;
		case 7:
			return 3;
		case 3:
		case 6:
		case 12: //force field - applies to bubbles
			return 4;
		default:
			MAIN.errorMessage("2112: Unknown damage type! " + eNum);
			return -1;
		}
	}

	public static String nameOfDamageType(int damageTypeNum)
	{
		switch (damageTypeNum)
		{
		case 0:
			return "Impact";
		case 1:
			return "Stab";
		case 2:
			return "Burn";
		case 3:
			return "Acid";
		case 4:
			return "Shock";
		case 9:
			return "Phantom";
		default:
			MAIN.errorMessage("GuillotineTit: Unknown damage type! " + damageTypeNum);
			return "WTF";
		}
	}

	public static int toInt(String e)
	{
		for (int i = 0; i < elementList.length; i++)
			if (elementList[i].equals(e))
				return i;
		return -1;
	}

	public static String[] elementList =
	{ "Fire", "Water", "Wind", "Electricity", "Metal", "Ice", "Energy", "Acid", "Lava", "Flesh", "Earth", "Plant", "Sense", "Strong", "Regenerate", "Flight", "Dexterity", "Armor", "Movement",
			"Teleport", "Ghost", "Force Field", "Time", "Loop", "Power", "Steal", "Audiovisual", "Summon", "Explosion", "Control", "Buff", "Charge" };

}
