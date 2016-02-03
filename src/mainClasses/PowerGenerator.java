package mainClasses;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PowerGenerator
{

	static Random				random	= new Random();
	public static String[][]	combos;					// [minor][MAJOR]
	public static String[][]	standalones;			// [element][level level (1-3, 4-6, 7-10)]

	public static List<Ability> generateAbilities(List<EP> EPs)
	{
		List<Ability> abilities = new ArrayList<Ability>();
		List<EP> majors = new ArrayList<EP>();
		List<EP> minors = new ArrayList<EP>();
		List<Integer> mmPairs = new ArrayList<Integer>();
		for (EP ep : EPs)
		{
			if (EP.elementList[ep.elementNum].equals("Charge")) // Charge can't be main unless it's 7-10
			{
				if (ep.points < 7)
					minors.add(ep);
				else
					majors.add(ep);
			} else if (ep.points > 3)
				majors.add(ep);
			else
				minors.add(ep);
		}
		if (majors.size() == 0)
			if (EP.toInt("Charge") == minors.get(0).elementNum)
				majors.add(minors.remove(1));
			else
				majors.add(minors.remove(0));
		// no interactions and weak interactions (main power remains, minor is dependent)
		for (int i = 0; i < minors.size(); i++)
		{
			EP minor = minors.get(i);
			int j = random.nextInt(majors.size());
			EP major = majors.get(j);
			int majplace = major.elementNum - 11;
			if (majplace < 0)
				majplace = 0;
			int minplace = minor.elementNum - 11;
			if (minplace < 0)
				minplace = 0;
			if (combos[minplace][majplace].equals("-"))
			{
				for (String s : choosePossibility(convertStringToPossibilityList(standalones[minplace][0])))
				{
					int elemental = 0;
					if (minplace == 0)
						elemental = minor.elementNum;
					s = s.replace("<E>", "<" + EP.elementList[elemental] + ">");
					addAbility(abilities, s, minor.points);
				}
				minors.remove(i);
				i--;
			} else if (!combos[minplace][majplace].startsWith("#"))
			{
				for (String s : choosePossibility(convertStringToPossibilityList(combos[minplace][majplace])))
				{
					int elemental = 0;
					if (minplace == 0)
						elemental = minor.elementNum;
					if (majplace == 0)
						elemental = major.elementNum;
					s = s.replace("<E>", "<" + EP.elementList[elemental] + ">");
					addAbility(abilities, s, minor.points);
				}
				minors.remove(i);
				i--;
			} else
				mmPairs.add(j);
		}

		// strong interactions
		for (int i = 0; i < minors.size() && !majors.isEmpty(); i++)
		{
			EP minor = minors.get(i);
			int j = mmPairs.get(i); // remembered for removal later
			EP major = majors.get(j);
			int majplace = major.elementNum - 11;
			if (majplace < 0)
				majplace = 0;
			int minplace = minor.elementNum - 11;
			if (minplace < 0)
				minplace = 0;
			for (String s : choosePossibility(convertStringToPossibilityList(combos[minplace][majplace].substring(1)))) // the substring is to remove that #
			{
				int elemental = 0;
				if (minplace == 0)
					elemental = minor.elementNum;
				if (majplace == 0)
					elemental = major.elementNum;
				s = s.replace("<E>", "<" + EP.elementList[elemental] + ">");
				addAbility(abilities, s, (minor.points + major.points));
			}
			minors.remove(i);
			majors.remove(j);
			mmPairs.remove(i);
			i--;
		}
		// leftover Main powers
		for (int i = 0; i < majors.size(); i++)
		{
			EP major = majors.get(i);
			int majplace = major.elementNum - 11;
			if (majplace < 0)
				majplace = 0;
			int rank = 1;
			if (major.points > 6)
				rank = 2;
			for (String s : choosePossibility(convertStringToPossibilityList(standalones[majplace][rank])))
			{
				int elemental = 0;
				if (majplace == 0)
					elemental = major.elementNum;
				s = s.replace("<E>", "<" + EP.elementList[elemental] + ">");
				addAbility(abilities, s, major.points);
			}
			majors.remove(i);
			i--;
		}
		//Sort the abilities first by points, and then by alphabetical name
		abilities.sort(Ability.pointsThenAlphabetical);
		return abilities;
	}

	static void addAbility(List<Ability> abilities, String s, int points)
	{
		switch (s)
		{ // SUBJECT TO CHANGE, THIS PART'S ACTUALLY CHANGING INSIDE THE CODE
		case "ff":
			switch (random.nextInt(5))
			{
			case 0:
				s = "Mobile Force Field";
				break;
			case 1:
				s = "Strong Force Field";
				break;
			case 2:
				s = "Wide Force Field";
				break;
			case 3:
				s = "Protective Bubble II";
				break;
			case 4:
				s = "Bubble";
				break;
			default:
				MAIN.errorMessage("A random number generator is doing wacky things!");
				break;
			}
			break;
		case "t1":
			switch (random.nextInt(2))
			{
			case 0:
				s = "Slow World";
				break;
			case 1:
				s = "Slow Target";
				break;
			default:
				MAIN.errorMessage("Haha, what?");
				break;
			}
			break;
		case "t2":
			switch (random.nextInt(3))
			{
			case 0:
				s = "Pause";
				break;
			case 1:
				s = "Time-Freeze Target I";
				break;
			case 2:
				s = "Chronobiology";
				break;
			default:
				MAIN.errorMessage("Ummmm this isn't supposed to happen");
				break;
			}
			break;
		case "t3":
			switch (random.nextInt(2))
			{
			case 0:
				s = "Time-Freeze Target II";
				break;
			case 1:
				s = "Slow-Motion";
				break;
			default:
				MAIN.errorMessage("lolwut");
				break;
			}
			break;
		case "l1":
			switch (random.nextInt(3))
			{
			case 0:
				s = "Repeat I";
				break;
			case 1:
				s = "Retrace I";
				break;
			case 2:
				s = "Undo I";
				break;
			default:
				MAIN.errorMessage("HAHAHAHAHAHAHAHAHAHAHAHAHA what?");
				break;
			}
			break;
		case "l2":
			switch (random.nextInt(3))
			{
			case 0:
				s = "Repeat II";
				break;
			case 1:
				s = "Retrace II";
				break;
			case 2:
				s = "Undo II";
				break;
			default:
				MAIN.errorMessage("This was totally supposed to happen?");
				break;
			}
			break;
		case "l3":
			switch (random.nextInt(3))
			{
			case 0:
				s = "Repeat III";
				break;
			case 1:
				s = "Retrace III";
				break;
			case 2:
				s = "Undo III";
				break;
			default:
				MAIN.errorMessage("I'm running out of original error messages here!");
				break;
			}
			break;
		case "buff":
			switch (random.nextInt(4))
			{
			case 0:
				s = "Buff Strength";
				break;
			case 1:
				s = "Buff Evasion";
				break;
			case 2:
				s = "Buff Armor";
				break;
			case 3:
				s = "Buff Power";
				break;
			default:
				MAIN.errorMessage("ERROR: SOMETHING WENT WRONG");
				break;
			}
			break;
		case "sense":
			switch (random.nextInt(7))
			{
			case 0:
				s = "Sense Life";
				break;
			case 1:
				s = "Sense Powers";
				break;
			case 2:
				s = "Sense Structure";
				break;
			case 3:
				s = "Sense Mana and Stamina";
				break;
			case 4:
				s = "Danger Sense";
				break;
			case 5:
				s = "Sense Movement";
				break;
			case 6:
				s = "Sense Parahumans";
				break;
			default:
				MAIN.errorMessage("ERROR: WHAT IS LIFE?");
				break;
			}
			break;
		case "noob": // For those rare cases when you actually need to choose a random equal power because you're too lazy to code a long complex solution
			if (points > 1)
				points /= 2;
			switch (random.nextInt(10))
			{
			case 0:
				s = "Sense Life";
				break;
			case 1:
				s = "Strength I";
				break;
			case 2:
				s = "Wound Regeneration I";
				break;
			case 3:
				s = "Flight I";
				break;
			case 4:
				s = "Precision I";
				break;
			case 5:
				s = "Toughness I";
				break;
			case 6:
				s = "Evasion I";
				break;
			case 7:
				s = "Blink";
				break;
			case 8:
				s = "Protective Bubble I";
				break;
			case 9:
				s = "Buff Strength";
				break;
			default:
				MAIN.errorMessage("INSUFFICIENT DATA FOR MEANINGFUL ANSWER");
				break;
			}
			break;
		default:
			//no error message here, this is actually what happens with like 70% of the abilities
			break;
		}
		if (s.startsWith("Resist Element") || s.startsWith("Absorb Element") || s.startsWith("Elemental Resistance"))
		{
			String element = s.substring(s.indexOf("<") + 1);
			element = element.substring(0, element.indexOf(">"));
			if (EP.damageType(element) == 0 || EP.damageType(element) == 1) // can't be resisted
			{
				addAbility(abilities, "noob", points); // Yes, that means that instead of Elemental Resistance <Metal>: 8, you'll get something random: 4.
				return;
			}
		}
		if (s.contains("I-III"))
		{
			String rank = "I";
			if (points > 3)
				rank += "I";
			if (points > 6)
				rank += "I";
			s = s.replace("I-III", rank);
		}
		if (s.contains("I-II")) // important that this test comes after the i-iii test
		{
			String rank = "I";
			if (points > 6)
				rank += "I";
			s = s.replace("I-II", rank);
		}
		Boolean alreadyExists = false;
		for (Ability a2 : abilities)
		{
			String s2 = a2.name;
			String type1 = s.replace("I", ""); // to remove ranks I-III
			String type2 = s2.replace("I", ""); // same
			if (type1.equals(type2))
				alreadyExists = true;
			// Yes, if there's a clash between a Rank II and a Rank III the Rank II will sometimes win. Sorry.
		}
		if (!alreadyExists)
		{
			abilities.add(Ability.ability(s, points));
		} else
		{
			addAbility(abilities, "noob", points);
		}
	}

	public static List<List<String>> convertStringToPossibilityList(String string)
	{
		if (string == null || string.length() == 0)
			return null;
		List<List<String>> possibilities = new ArrayList<List<String>>();
		String posString = "";
		String power = "";
		List<String> posStringList = new ArrayList<String>();
		for (int i = 0; i < string.length(); i++)
		{
			if (string.charAt(i) == '/')
			{
				posStringList.add(posString);
				posString = "";
			} else
				posString += string.charAt(i);
		}
		posStringList.add(posString);
		for (int i = 0; i < posStringList.size(); i++)
		{
			List<String> possibility = new ArrayList<String>(); // A number of powers
			posString = posStringList.get(i); // a number of powers, in a single string
			power = ""; // a single power
			for (int j = 0; j < posString.length(); j++)
			{
				if (posString.charAt(j) == '+')
				{
					possibility.add(power);
					power = "";
				} else
					power += posString.charAt(j);
			}
			possibility.add(power);
			possibilities.add(possibility);
		}
		return possibilities;
	}

	public static List<String> choosePossibility(List<List<String>> possibilities)
	{
		return possibilities.get(random.nextInt(possibilities.size()));
	}

	public static void initializeTables()
	{
		try
		{
			// combinations
			BufferedReader in = new BufferedReader(new InputStreamReader(PowerGenerator.class.getResourceAsStream("combinations.csv"), "UTF-8"));
			if (!in.ready())
			{
				MAIN.errorMessage("EMPTY FILE - COMBINATIONS");
				in.close();
				return;
			}
			int n = 1; // n should be number of elements
			String line = in.readLine();
			for (int j = 0; j < line.length(); j++)
				if (line.charAt(j) == ',')
					n++;
			combos = new String[n][n];
			in.close();
			in = new BufferedReader(new InputStreamReader(Ability.class.getResourceAsStream("combinations.csv"), "UTF-8"));
			String cell = "";
			for (int i = 0; i < n; i++) // alternatively: while (in.ready())
			{
				line = in.readLine();
				cell = "";
				int j = 0;
				for (int k = 0; k < line.length(); k++)
					if (line.charAt(k) == ',')
					{
						combos[i][j] = cell;
						j++;
						cell = "";
					} else
						cell += line.charAt(k);
				combos[i][j] = cell;
			}

			// standalones
			in.close();
			in = new BufferedReader(new InputStreamReader(Ability.class.getResourceAsStream("standalones.csv"), "UTF-8"));
			if (!in.ready())
			{
				MAIN.errorMessage("EMPTY FILE - STANDALONES");
				in.close();
				return;
			}
			standalones = new String[n][3];
			cell = "";
			int i = 0;
			while (in.ready())
			{
				line = in.readLine();// .substring(1); // sometimes, for some bizarre reason, every line starts with a comma.
				cell = "";
				int j = 0;
				for (int k = 0; k < line.length(); k++)
					if (line.charAt(k) == ',')
					{
						standalones[i][j] = cell;
						j++;
						cell = "";
					} else
						cell += line.charAt(k);
				standalones[i][j] = cell;
				i++;
			}
			in.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			MAIN.errorMessage("(there was a bug)");
		}
		;
	}
}
