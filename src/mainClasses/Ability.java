package mainClasses;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import abilities.Ball_E;
import abilities.Beam_E;
import abilities.Blink;
import abilities.Elemental_Combat_II_E;
import abilities.Elemental_Combat_I_E;
import abilities.Elemental_Void;
import abilities.Flight_I;
import abilities.Flight_II;
import abilities.Force_Shield;
import abilities.Ghost_Mode_I;
import abilities.Heal_I;
import abilities.Heal_II;
import abilities.Pool_E;
import abilities.Precision_I;
import abilities.Protective_Bubble_I;
import abilities.Punch;
import abilities.Ranged_Explosion;
import abilities.Sense_Element_E;
import abilities.Sense_Life;
import abilities.Sense_Mana_and_Stamina;
import abilities.Sense_Movement;
import abilities.Sense_Parahumans;
import abilities.Sense_Powers;
import abilities.Sense_Structure;
import abilities.Shield_E;
import abilities.Spray_E;
import abilities.Sprint;
import abilities.Strength_I;
import abilities.Strength_II;
import abilities.Strength_III;
import abilities.Strike_E;
import abilities.Strong_Force_Field;
import abilities.Telekinetic_Flight;
import abilities.Toughness_III;
import abilities.Wall_E;
import mainResourcesPackage.SoundEffect;

public class Ability
{
	protected static List<String>	descriptions				= new ArrayList<String>();
	protected static boolean[][]	elementalAttacksPossible	= new boolean[12][7];												// [element][ability]
	protected static int[][]		elementalAttackNumbers		= new int[12][3];
	protected static String[]		elementalAttacks			= new String[]
																	{ "Ball", "Beam", "Shield", "Wall", "Spray", "Strike", "Pool" };

	// permanent variables of the ability
	protected String				name;																							// name of the ability
	protected int					level;																							// 1-10. AKA "level". Measures how powerful the ability is.
	protected double				cooldown;																						// Duration in which power doesn't work after using it. -1 = passive, 0 = no cooldown
	protected double				cost;																							// -1 = passive. Is a cost in mana, stamina or charge...depending on the power.
	protected double				costPerSecond;																					// applies to some abilities. Is a cost in mana, stamina or charge...depending on the power.
	protected int					range;																							// distance from user in which ability can be used. For some abilities - how far they go before stopping. -1 = not
																																	// ranged, or only
	// direction-aiming.
	protected double				areaRadius;																						// radius of area of effect of ability.
	protected boolean				instant;																						// Instant abilities don't aim, they immediately activate after a single click. Maintained abilities are always
																																	// instant.
	protected boolean				maintainable;																					// Maintained abilities are instant, and require you to continue holding the button to use them (they're continuous
																																	// abilities).
	protected boolean				stopsMovement;																					// Does the power stop the person from moving?
	protected String				costType;																						// "none", "mana", "stamina", "charge" or "life". Abilities that use multiple don't exist, I think.
	public double					arc;																							// used for abilities with an arc - the Spray ability

	// changing variables of the ability
	protected double				timeLeft;																						// how much time the ability has been on.
	protected double				cooldownLeft;																					// if cooldown is -1 (passive), then a cooldownLeft of 0 means the ability hasn't been initialized yet
	protected boolean				on;																								// For maintained and on/off abilities - whether or not the power is active.
	protected int					elementNum;

	protected String[]				tags;																							// list of tags.
	// possible tags are:
	// offensive, projectile

	// EXAMPLES
	//
	// Fire Beam 7: name = "Beam <Fire>"; points = 7; cooldown = 0.5; cost = 0; costPerSecond = 5 / <fire damage>; range = 500*points; areaRadius = -1; instant = true; maintainable = true; stopsMovement = false; onOff = false; costType = "mana";

	// for special effects:
	protected String				rangeType					= "";
	protected int					frameNum					= 0;																// used in child classes

	protected List<SoundEffect>		sounds						= new ArrayList<SoundEffect>();

	@SuppressWarnings("unused")
	public void use(Environment env, Person user, Point target)
	{
		// to be written in child classes
		Main.errorMessage("DANGER WARNING LEVEL - DEMON. NO USE METHOD FOUND FOR THIS ABILITY: " + name);
	}

	@SuppressWarnings("unused")
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		// to be written in child classes
		Main.errorMessage("A man, a plan, a canal, Panama. no maintain for this ability. " + name);
	}

	@SuppressWarnings("unused")
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		// to be written in child classes
		Main.errorMessage("Time Wroth Lime Broth Crime Froth Grime Sloth.. no updatePlayerTargeting for this ability. " + name);
	}

	public Ability(String n, int p)
	{
		name = n;
		level = p;

		// default values.
		costPerSecond = -1;
		cooldown = -1;
		cooldownLeft = 0;
		cost = -1;
		costPerSecond = -1;
		range = -1;
		areaRadius = -1;
		instant = false;
		maintainable = false;
		stopsMovement = false;
		on = false;
		costType = "none";
		timeLeft = 0;

		addTags();
		elementNum = getElementNum();
	}

	void addTags()
	{
		// tags
		tagloop:
		{
			for (String s : Ability.descriptions)
				if (s.startsWith(justName()) && s.charAt(justName().length()) == ' ')
				{
					String text = getDescription(name);
					if (text.indexOf("\n") == -1)
						Main.errorMessage("ability class go to this line and solve this. name was " + name + " and text was: " + text);
					text = text.substring(text.indexOf("\n") + 1, text.indexOf("\n", text.indexOf("\n") + 1)); // skip first line, delete fluff and description
					tag(text);
					break tagloop;
				}
			Main.errorMessage("[Ablt] There has been no tag found for the ability:   " + name);
		}
	}

	private void tag(String tagList)
	{
		// taglist is a string like "dangerous fire symbolic" which is parsed and will create a tag array of {"dangerous", "fire", "symbolic"}
		// make sure not to include double spaces!
		if (tagList.length() < 1)
		{
			tags = new String[0];
			return;
		}
		List<String> tags2 = new ArrayList<String>();
		String currTag = "";
		for (int i = 0; i < tagList.length(); i++)
		{
			if (tagList.charAt(i) != ' ')
				currTag += tagList.charAt(i);
			else
			{
				tags2.add(currTag);
				currTag = "";
			}
		}
		tags2.add(currTag);
		tags = new String[tags2.size()];
		for (int i = 0; i < tags2.size(); i++)
			tags[i] = tags2.get(i);
	}

	public boolean hasTag(String tag)
	{
		if (tags.length == 0)
			return false;
		for (int i = 0; i < tags.length; i++)
			if (tags[i].equals(tag))
				return true;
		return false;
	}

	public String getTags()
	{
		String s = "";
		for (int i = 0; i < tags.length; i++)
			s += " " + tags[i];
		return s.substring(1);
	}

	public void stopAllSounds()
	{
		for (int i = 0; i < sounds.size(); i++)
			sounds.get(i).stop();
	}

	public static String getName(String text)
	{
		// no element, no description or fluff
		return text.substring(0, text.indexOf("(") - 1);
		// Will give an error message if there is no "(" in the ability's name (or text), so when that happens insert a printing function here to see where you forgot a newline or something
	}

	public String getElement()
	{
		if (name.contains("<"))
			return name.substring(name.indexOf("<") + 1, name.indexOf(">"));
		return "NONE";
	}

	public int getElementNum()
	{
		if (name.contains("<"))
			return EP.toInt(getElement());
		return -1;
	}

	public String justName()
	{
		if (name.contains("<"))
			return name.substring(0, name.indexOf("<") - 1);
		return name;
	}

	public static String getDescription(String name)
	{
		// name must not contain any numbers or elements
		// this method's returned string contains <E>
		if (name.contains("<"))
		{
			for (int i = 0; i < descriptions.size(); i++)
				if (descriptions.get(i).substring(0, descriptions.get(i).indexOf('(') - 1).equals(name.substring(0, name.indexOf("<") - 1)))
					return descriptions.get(i);
		} else
			for (int i = 0; i < descriptions.size(); i++)
				if (descriptions.get(i).substring(0, descriptions.get(i).indexOf('(') - 1).equals(name))
					return descriptions.get(i);
		return "String not found in abilities: " + name;

	}

	public String niceName()
	{ // turns "Ball <Fire>" into "Fire Ball"
		if (name.contains("<"))
			return name.substring(name.indexOf("<") + 1, name.indexOf(">")) + " " + name.substring(0, name.indexOf("<") - 1);
		return name;
	}

	public String getFluff()
	{
		return Ability.getFluff(name);
	}

	public static String getFluff(String ability)
	{
		String realName = ability;
		String element = "0";
		if (ability.contains("<"))
		{
			realName = ability.substring(0, ability.indexOf("<") - 1);
			element = ability.substring(ability.indexOf("<") + 1, ability.indexOf(">")).toLowerCase();
		}
		String text = getDescription(ability);
		if (text == null)
			return "<no ability found with the name \"" + realName + "\".>";

		// flesh stuff :)
		if (element.equals("flesh"))
		{
			if (realName.contains("Combat"))
				element = "flesh, bone and blood";
			else if (realName.contains("Beam"))
				element = "blood";
			else if (realName.contains("Ball"))
				element = "meat";
			else if (realName.contains("Shield"))
				element = "meat";
			else if (realName.contains("Spray"))
				element = "bones";
			else if (realName.contains("Strike"))
				element = "skeletally-enhanced";
			else if (realName.contains("Sense"))
				text = "Sense Element (Passive) <Flesh>\nSense blood pools, bone walls, meat shields, and people with Flesh powers.\nSee silhouettes of capes with your elemental power, walls/pools of your element or creatures under your element’s effect, and know how hurt they are. The range is 3^Level. Sense Element <Earth> allows you to also have Sense Structure! Yup!";
			else if (realName.contains("Strike"))
				element = "skeletally-enhanced";
			else if (realName.contains("Fists"))
				element = "strengthened bones";
			else if (realName.contains("Chemtrail"))
				element = "bloody";
			else if (realName.contains("Armor")) // It's a flesh armor, that's what I decided
				element = "flesh";
			else if (realName.contains("Trail"))
				element = "bloody";
			else if (realName.contains("Teletrail"))
				element = "flesh";
			else if (realName.contains("Teleport"))
				element = "bones, blood or meat (that aren't part of a human being)";
			else if (realName.contains("Theft"))
				element = "Flesh-related";
			else if (realName.contains("Summoning"))
				element = "flesh";
			else if (realName.contains("Explosions"))
				element = "flesh and gore! :D";
			else if (realName.contains("Specialty"))
				element = "flesh (as a power)";
			else if (realName.contains("Reshape"))
				element = "bones, blood and meat";
			else if (realName.contains("Pool"))
				element = "blood";
			else if (realName.contains("Melt"))
				text = "Melt (Activated) <Flesh>\nMelt a bone wall into a blood pool. Because powers don't always make sense.\nTarget an <Element> wall. Destroy it and create an <Element> pool instead.\nCost: 0 Mana.";
			else if (realName.contains("Launch"))
				text = "Launch Wall (Activated) <Flesh>\nTransform a bone wall into a meat ball through sheer force of will, and then throw it at a target.\nTarget a wall and then a creature. The wall immediately becomes an <Element> Ball and is thrown towards the target.\nCost: 2 Mana.";
			else if (realName.contains("Wall")) // important that this is after "Launch"!
				element = "bone";
			else
				element = "BUG (Flesh) BUG";
		}
		text = text.substring(text.indexOf("\n") + 1); // skip name and type
		text = text.substring(text.indexOf("\n") + 1, text.indexOf("\n", text.indexOf("\n") + 1)); // skip tags, delete description
		text = text.replace("<e>", element);
		switch (element.charAt(0))
		// Add "a"/"an" depending on if element starts with a vowel. This part of method might need to move to EP.
		{
		case 'a':
		case 'e':
		case 'i':
		case 'o':
		case 'u':
			text = text.replace("<an e>", "an " + element);
			break;
		default:
			text = text.replace("<an e>", "a " + element);
			break;
		}
		return text;
	}

	public void setSounds(Point point)
	{
		for (SoundEffect s: sounds)
			s.setPosition(point);
	}
	
	public static void initializeDescriptions()
	{
		try
		{
			// combinations
			BufferedReader in = new BufferedReader(new InputStreamReader(Ability.class.getResourceAsStream("abilities.txt"), "UTF-8"));
			if (!in.ready())
			{
				Main.errorMessage("EMPTY FILE - ABILITIES");
				in.close();
				return;
			}
			in.read();
			while (in.ready())
			{
				String s = "";
				String currLine = in.readLine();
				while (currLine != null && !currLine.equals(""))
				{
					s += currLine + "\n";
					currLine = in.readLine();
				}
				currLine = in.readLine(); // extra paragraph break
				s = s.substring(0, s.length() - 1); // remove final paragraph break
				descriptions.add(s);
			}

			// elemental attacks possible
			in = new BufferedReader(new InputStreamReader(Ability.class.getResourceAsStream("elementalCombatPossibilities.csv"), "UTF-8"));
			if (!in.ready())
			{
				Main.errorMessage("EMPTY FILE - ELEMENTAL COMBAT ATTACKS");
				in.close();
				return;
			}
			String line = "";
			int i = 0;
			while (in.ready())
			{
				line = in.readLine();
				int j = 0;
				for (int k = 0; k < line.length(); k++)
					switch (line.charAt(k))
					{
					case 'X':
						elementalAttacksPossible[i][j] = true;
						j++;
						break;
					case 'O':
						elementalAttacksPossible[i][j] = false;
						j++;
						break;
					case ',':
						break;
					default: // Damage and stuff
						elementalAttackNumbers[i][j - 7] = Integer.parseInt("" + line.charAt(k));
						j++;
						break;
					}
				i++;
			}
			in.close();
		} catch (IOException e)
		{
			e.printStackTrace();
			Main.errorMessage("(there was a bug, I think)");
		}
	}

	public String toString()
	{
		return name + " [" + level + "]";
	}

	static Comparator<Ability> pointsThenAlphabetical = new Comparator<Ability>()
	{
		public int compare(Ability a1, Ability a2)
		{
			if (a1.level != a2.level)
				return Integer.compare(a2.level, a1.level);
			else
				return a1.name.compareTo(a2.name);
		}
	};

	public static Ability ability(String abilityName, int pnts)
	{
		String element = null;
		String trimmedAbilityName = abilityName;
		if (abilityName.indexOf("<") != -1)
		{
			element = abilityName.substring(abilityName.indexOf("<") + 1, abilityName.indexOf(">"));
			trimmedAbilityName = abilityName.substring(0, abilityName.indexOf("<") - 1);
		}
		switch (trimmedAbilityName)
		{
		case "Elemental Void":
			return new Elemental_Void(pnts);
		case "Precision I":
			return new Precision_I(pnts);
		case "Protective Bubble I":
			return new Protective_Bubble_I(pnts);
		case "Sprint":
			return new Sprint(pnts);
		case "Strength I":
			return new Strength_I(pnts);
		case "Strength II":
			return new Strength_II(pnts);
		case "Strength III":
			return new Strength_III(pnts);
		case "Punch":
			return new Punch(pnts);
		case "Heal I":
			return new Heal_I(pnts);
		case "Heal II":
			return new Heal_II(pnts);
		case "Force Shield":
			return new Force_Shield(pnts);
		case "Ranged Explosion":
			return new Ranged_Explosion(pnts);
		case "Flight I":
			return new Flight_I(pnts);
		case "Flight II":
			return new Flight_II(pnts);
		case "Telekinetic Flight":
			return new Telekinetic_Flight(pnts);
		case "Blink":
			return new Blink(pnts);
		case "Ghost Mode I":
			return new Ghost_Mode_I(pnts);
		case "Strong Force Field":
			return new Strong_Force_Field(pnts);
		case "Beam":
			return new Beam_E(element, pnts);
		case "Ball":
			return new Ball_E(element, pnts);
		case "Shield":
			return new Shield_E(element, pnts);
		case "Pool":
			return new Pool_E(element, pnts);
		case "Wall":
			return new Wall_E(element, pnts);
		case "Spray":
			return new Spray_E(element, pnts);
		case "Strike":
			return new Strike_E(element, pnts);
		case "Toughness III":
			return new Toughness_III(pnts);
		case "Sense Life":
			return new Sense_Life(pnts);
		case "Sense Mana and Stamina":
			return new Sense_Mana_and_Stamina(pnts);
		case "Sense Movement":
			return new Sense_Movement(pnts);
		case "Sense Structure":
			return new Sense_Structure(pnts);
		case "Sense Parahumans":
			return new Sense_Parahumans(pnts);
		case "Sense Powers":
			return new Sense_Powers(pnts);
		case "Sense Element":
			return new Sense_Element_E(element, pnts);
		case "Elemental Combat I":
			return new Elemental_Combat_I_E(element, pnts);
		case "Elemental Combat II":
			return new Elemental_Combat_II_E(element, pnts);
		default:
			Main.errorMessage("Donald trump peninsula error - " + abilityName);

			return null;
		// Just because the game isn't finished yet and I still haven't made all 151 ability methods:
		}
	}
}