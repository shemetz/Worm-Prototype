package mainClasses;

import java.awt.Point;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import abilities.Ball_E;
import abilities.Beam_E;
import abilities.Blink;
import abilities.Chronobiology;
import abilities.Clone_I;
import abilities.Clone_II;
import abilities.Clone_III;
import abilities.Danger_Sense;
import abilities.Elemental_Combat_II_E;
import abilities.Elemental_Combat_I_E;
import abilities.Elemental_Fists_E;
import abilities.Elemental_Resistance_E;
import abilities.Elemental_Void;
import abilities.Exploding_Fists;
import abilities.Flight_I;
import abilities.Flight_II;
import abilities.Force_Shield;
import abilities.Ghost_Mode_I;
import abilities.Heal_I;
import abilities.Heal_II;
import abilities.Nullification_Aura_I;
import abilities.Nullification_Aura_II;
import abilities.Pool_E;
import abilities.Portals;
import abilities.Precision_I;
import abilities.Precision_II;
import abilities.Precision_III;
import abilities.Protective_Bubble_I;
import abilities.Punch;
import abilities.Pushy_Fists;
import abilities.Ranged_Explosion;
import abilities.Repeat_I;
import abilities.Repeat_II;
import abilities.Repeat_III;
import abilities.Retrace_I;
import abilities.Retrace_II;
import abilities.Retrace_III;
import abilities.Sapping_Fists;
import abilities.Sense_Element_E;
import abilities.Sense_Life;
import abilities.Sense_Mana_and_Stamina;
import abilities.Sense_Movement;
import abilities.Sense_Parahumans;
import abilities.Sense_Powers;
import abilities.Sense_Structure;
import abilities.Shattering_Fists;
import abilities.Shield_E;
import abilities.Slow_Target;
import abilities.Spray_E;
import abilities.Sprint;
import abilities.Steal_Power;
import abilities.Strength_I;
import abilities.Strength_II;
import abilities.Strength_III;
import abilities.Strike_E;
import abilities.Strong_Force_Field;
import abilities.Telekinetic_Flight;
import abilities.Time_Freeze_Target_I;
import abilities.Toughness_I;
import abilities.Toughness_II;
import abilities.Toughness_III;
import abilities.Twitch;
import abilities.Undo_I;
import abilities.Undo_II;
import abilities.Undo_III;
import abilities.Vampiric_Fists;
import abilities.Wall_E;
import abilities.Wild_Power;
import mainResourcesPackage.SoundEffect;

public class Ability
{
	final static List<String> implementedAbilities = Arrays.asList("Elemental Combat I", "Beam", "Ball", "Shield", "Pool", "Wall", "Spray", "Sense Element", "Elemental Resistance", "Strike",
			"Portals", "Elemental Void", "Precision I", "Precision II", "Precision III", "Protective Bubble I", "Sprint", "Strength I", "Strength II", "Strength III", "Punch", "Heal I", "Heal II",
			"Force Shield", "Ranged Explosion", "Flight I", "Flight II", "Telekinetic Flight", "Blink", "Ghost Mode I", "Strong Force Field", "Toughness I", "Toughness II", "Toughness III",
			"Sense Life", "Sense Mana and Stamina", "Sense Powers", "Slow Target", "Chronobiology", "Retrace I", "Retrace II", "Retrace III", "Undo I", "Undo II", "Undo III", "Repeat I", "Repeat II",
			"Repeat III", "Time Freeze Target I", "Nullification Aura I", "Nullification Aura II", "Wild Power", "Clone I", "Clone II", "Clone III", "Twitch", "Sense Structure", "Sense Parahumans",
			"Steal Power", "Danger Sense", "Sapping Fists", "Pushy Fists", "Exploding Fists", "Vampiric Fists", "Shattering Fists", "Elemental Fists");
	protected static List<String> descriptions = new ArrayList<String>();
	protected static boolean[][] elementalAttacksPossible = new boolean[12][7]; // [element][ability]
	protected static int[][] elementalAttackNumbers = new int[12][3];
	protected static String[] elementalAttacks = new String[]
	{ "Ball", "Beam", "Shield", "Wall", "Spray", "Strike", "Pool" };
	final static List<String> elementalPowers = Arrays.asList("Elemental Combat I", "Elemental Combat II", "Ball", "Beam", "Shield", "Wall", "Spray", "Strike", "Pool", "Sense Element",
			"Elemental Resistance", "Elemental Fists");

	public enum CostType
	{
		NONE, MANA, STAMINA, CHARGE, LIFE
	}; // Abilities that use multiple don't exist, I think.

	public enum RangeType
	{
		CREATE_IN_GRID, EXACT_RANGE, CIRCLE_AREA, CONE, NONE
	};

	// permanent variables of the ability
	public String name; // name of the ability
	public int level; // 1-10. AKA "level". Measures how powerful the ability is.
	protected double cooldown; // Duration in which power doesn't work after using it. -1 = passive, 0 = no cooldown
	protected double cost; // -1 = passive. Is a cost in mana, stamina or charge...depending on the power.
	protected double costPerSecond; // applies to some abilities. Is a cost in mana, stamina or charge...depending on the power.
	protected int range; // distance from user in which ability can be used. For some abilities - how far they go before stopping. -1 = not ranged, or only direction-aiming.
	protected double areaRadius; // radius of area of effect of ability.
	protected boolean instant; // Instant abilities don't aim, they immediately activate after a single click. Maintained abilities are always instant.
	public boolean toggleable;
	protected boolean maintainable; // Maintained abilities are instant, and require you to continue holding the button to use them (they're continuous abilities).
	protected boolean stopsMovement; // Does the power stop the person from moving?
	protected CostType costType;
	public double arc; // used for abilities with an arc - the Spray ability
	public boolean natural;
	public double damage;
	public double pushback;
	public double steal;

	// changing variables of the ability
	protected double timeLeft; // how much time the ability has been on.
	protected double cooldownLeft; // if cooldown is -1 (passive), then a cooldownLeft of 0 means the ability hasn't been initialized yet
	public boolean on; // For maintained and on/off abilities - whether or not the power is active.
	public int elementNum;
	public boolean disabled;
	public boolean prepareToDisable;
	public boolean prepareToEnable;

	protected String[] tags; // list of tags.
	// possible tags are:
	// offensive, projectile

	// EXAMPLES
	//
	// Fire Beam 7: name = "Beam <Fire>"; points = 7; cooldown = 0.5; cost = 0; costPerSecond = 5 / <fire damage>; range = 500*points; areaRadius = -1; instant = true; maintainable = true; stopsMovement = false; onOff = false; costType =
	// CostType.MANA;

	// for special effects:
	protected RangeType rangeType;
	protected int frameNum = 0; // used in child classes

	protected List<SoundEffect> sounds = new ArrayList<SoundEffect>();

	@SuppressWarnings("unused")
	public void use(Environment env, Person user, Point target)
	{
		// to be written in child classes
		MAIN.errorMessage("DANGER WARNING LEVEL - DEMON. NO USE METHOD FOUND FOR THIS ABILITY: " + name);
	}

	@SuppressWarnings("unused")
	public void maintain(Environment env, Person user, Point target, double deltaTime)
	{
		// to be written in child classes
		MAIN.errorMessage("A man, a plan, a canal, Panama. no maintain for this ability. " + name);
	}

	@SuppressWarnings("unused")
	public void updatePlayerTargeting(Environment env, Player player, Point target, double deltaTime)
	{
		// to be written in child classes
		MAIN.errorMessage("Time Wroth Lime Broth Crime Froth Grime Sloth.. no updatePlayerTargeting for this ability. " + name);
	}

	@SuppressWarnings("unused")
	public void disable(Environment env, Person user)
	{
		// to be written in child classes
		MAIN.errorMessage("vjhvsfasetjblckvzyuf no disable() for this method. " + name);
	}

	public Ability clone()
	{
		Ability clone = Ability.ability(this.name, this.level);
		if (this.hasTag("on-off"))
			clone.on = this.on;

		return clone;
	}

	public Ability(String n, int p)
	{
		name = n;
		level = p;

		// default values.
		costPerSecond = -1;
		cooldown = 0;
		cooldownLeft = 0;
		cost = -1;
		costPerSecond = -1;
		range = -1;
		areaRadius = -1;
		instant = false;
		maintainable = false;
		stopsMovement = false;
		toggleable = false;
		on = false;
		costType = CostType.NONE;
		rangeType = RangeType.NONE;
		timeLeft = 0;
		disabled = false;
		prepareToDisable = false;
		prepareToEnable = false;
		natural = false;

		addTags();
		elementNum = getElementNum();
	}

	public void toggle()
	{
		MAIN.errorMessage("Toggleable ability was toggled, but the toggle method that toggled was not overridden. toggle.  (ability is " + name + ")");
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
						MAIN.errorMessage("ability class go to this line and solve this. name was " + name + " and text was: " + text);
					text = text.substring(text.indexOf("\n") + 1, text.indexOf("\n", text.indexOf("\n") + 1)); // skip first line, delete fluff and description
					tag(text);
					break tagloop;
				}
			MAIN.errorMessage("[Ablt] There has been no tag found for the ability:   " + name);
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
		if (text.indexOf("(") == -1)
			MAIN.errorMessage("PR-O-BLEM   no name for " + text);
		return text.substring(0, text.indexOf("(") - 1);
		// Will give an error message if there is no "(" in the ability's name (or text), so when that happens insert a printing function here to see where you forgot a newline or something
	}

	public String getElement()
	{
		if (name.contains("<"))
			return name.substring(name.indexOf("<") + 1, name.indexOf(">"));
		return "NONE";
	}

	private int getElementNum()
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

	public static String justName(String name)
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
		}
		else
			for (int i = 0; i < descriptions.size(); i++)
				if (descriptions.get(i).substring(0, descriptions.get(i).indexOf('(') - 1).equals(name))
					return descriptions.get(i);
		return "String not found in abilities: " + name;

	}

	public static String niceName(String name)
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
			else if (realName.contains("Sense"))
				text = "Sense Element (Passive) <Flesh>\nSense blood pools, bone walls, meat shields, and people with Flesh powers.\ntags\nSee silhouettes of capes with your elemental power, walls/pools of your element or creatures under your element’s effect, and know how hurt they are. The range is 3^Level. Sense Element <Earth> allows you to also have Sense Structure! Yup!";
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
			else if (realName.contains("Pool"))
				element = "blood";
			else if (realName.contains("Wall"))
				element = "bone";
			else
				element = "BUG (Flesh) BUG";
		}
		if (element.equals("plant"))
			if (realName.contains("Beam")) // TODO fix this. it does not work.
				text.replace("continuous beam of plant", "vine that can grab onto stuff");

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
		for (SoundEffect s : sounds)
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
				MAIN.errorMessage("EMPTY FILE - ABILITIES");
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
				s = s.substring(0, s.length() - 1); // remove final paragraph break
				descriptions.add(s);
			}

			// elemental attacks possible
			in = new BufferedReader(new InputStreamReader(Ability.class.getResourceAsStream("elementalCombatPossibilities.csv"), "UTF-8"));
			if (!in.ready())
			{
				MAIN.errorMessage("EMPTY FILE - ELEMENTAL COMBAT ATTACKS");
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
		}
		catch (IOException e)
		{
			e.printStackTrace();
			MAIN.errorMessage("(there was a bug, I think)");
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

	public void init()
	{
		if (cooldown == -1) // todo remove this
			System.out.println(this.name);
		// make sure all values are OK
		level = Math.min(level, 10);
		level = Math.max(1, level);
		cooldown = Math.max(0, cooldown);
		cost = Math.max(0, cost);
		costPerSecond = Math.max(0, costPerSecond);
		range = Math.max(0, range);
		areaRadius = Math.max(0, areaRadius);
		arc = Math.max(0, arc);
	}

	public static Ability ability(String abilityName, int pnts)
	{
		Ability ab = null;

		String element = null;
		String trimmedAbilityName = abilityName;
		if (abilityName.indexOf("<") != -1)
		{
			element = abilityName.substring(abilityName.indexOf("<") + 1, abilityName.indexOf(">"));
			trimmedAbilityName = abilityName.substring(0, abilityName.indexOf("<") - 1);
		}
		if (!implementedAbilities.contains(trimmedAbilityName))
		{
			MAIN.errorMessage();
			return null;
		}
		switch (trimmedAbilityName)
		{
		case "Exploding Fists":
			ab = new Exploding_Fists(pnts);
			break;
		case "Vampiric Fists":
			ab = new Vampiric_Fists(pnts);
			break;
		case "Shattering Fists":
			ab = new Shattering_Fists(pnts);
			break;
		case "Pushy Fists":
			ab = new Pushy_Fists(pnts);
			break;
		case "Sapping Fists":
			ab = new Sapping_Fists(pnts);
			break;
		case "Elemental Fists":
			ab = new Elemental_Fists_E(element, pnts);
			break;
		case "Steal Power":
			ab = new Steal_Power(pnts);
			break;
		case "Twitch":
			ab = new Twitch(pnts);
			break;
		case "Clone I":
			ab = new Clone_I(pnts);
			break;
		case "Clone II":
			ab = new Clone_II(pnts);
			break;
		case "Clone III":
			ab = new Clone_III(pnts);
			break;
		case "Wild Power":
			ab = new Wild_Power(pnts);
			break;
		case "Nullification Aura I":
			ab = new Nullification_Aura_I(pnts);
			break;
		case "Nullification Aura II":
			ab = new Nullification_Aura_II(pnts);
			break;
		case "Time Freeze Target I":
			ab = new Time_Freeze_Target_I(pnts);
			break;
		case "Repeat I":
			ab = new Repeat_I(pnts);
			break;
		case "Repeat II":
			ab = new Repeat_II(pnts);
			break;
		case "Repeat III":
			ab = new Repeat_III(pnts);
			break;
		case "Undo I":
			ab = new Undo_I(pnts);
			break;
		case "Undo II":
			ab = new Undo_II(pnts);
			break;
		case "Undo III":
			ab = new Undo_III(pnts);
			break;
		case "Retrace I":
			ab = new Retrace_I(pnts);
			break;
		case "Retrace II":
			ab = new Retrace_II(pnts);
			break;
		case "Retrace III":
			ab = new Retrace_III(pnts);
			break;
		case "Chronobiology":
			ab = new Chronobiology(pnts);
			break;
		case "Portals":
			ab = new Portals(pnts);
			break;
		case "Elemental Void":
			ab = new Elemental_Void(pnts);
			break;
		case "Precision I":
			ab = new Precision_I(pnts);
			break;
		case "Precision II":
			ab = new Precision_II(pnts);
			break;
		case "Precision III":
			ab = new Precision_III(pnts);
			break;
		case "Protective Bubble I":
			ab = new Protective_Bubble_I(pnts);
			break;
		case "Sprint":
			ab = new Sprint(pnts);
			break;
		case "Strength I":
			ab = new Strength_I(pnts);
			break;
		case "Strength II":
			ab = new Strength_II(pnts);
			break;
		case "Strength III":
			ab = new Strength_III(pnts);
			break;
		case "Punch":
			ab = new Punch(pnts);
			break;
		case "Heal I":
			ab = new Heal_I(pnts);
			break;
		case "Heal II":
			ab = new Heal_II(pnts);
			break;
		case "Force Shield":
			ab = new Force_Shield(pnts);
			break;
		case "Ranged Explosion":
			ab = new Ranged_Explosion(pnts);
			break;
		case "Flight I":
			ab = new Flight_I(pnts);
			break;
		case "Flight II":
			ab = new Flight_II(pnts);
			break;
		case "Telekinetic Flight":
			ab = new Telekinetic_Flight(pnts);
			break;
		case "Blink":
			ab = new Blink(pnts);
			break;
		case "Ghost Mode I":
			ab = new Ghost_Mode_I(pnts);
			break;
		case "Strong Force Field":
			ab = new Strong_Force_Field(pnts);
			break;
		case "Beam":
			ab = new Beam_E(element, pnts);
			break;
		case "Ball":
			ab = new Ball_E(element, pnts);
			break;
		case "Shield":
			ab = new Shield_E(element, pnts);
			break;
		case "Pool":
			ab = new Pool_E(element, pnts);
			break;
		case "Wall":
			ab = new Wall_E(element, pnts);
			break;
		case "Spray":
			ab = new Spray_E(element, pnts);
			break;
		case "Strike":
			ab = new Strike_E(element, pnts);
			break;
		case "Elemental Resistance":
			ab = new Elemental_Resistance_E(element, pnts);
			break;
		case "Toughness I":
			ab = new Toughness_I(pnts);
			break;
		case "Toughness II":
			ab = new Toughness_II(pnts);
			break;
		case "Toughness III":
			ab = new Toughness_III(pnts);
			break;
		case "Sense Life":
			ab = new Sense_Life(pnts);
			break;
		case "Sense Mana and Stamina":
			ab = new Sense_Mana_and_Stamina(pnts);
			break;
		case "Sense Movement": // NOT DONE
			ab = new Sense_Movement(pnts);
			break;
		case "Danger Sense":
			ab = new Danger_Sense(pnts);
			break;
		case "Sense Structure":
			ab = new Sense_Structure(pnts);
			break;
		case "Sense Parahumans":
			ab = new Sense_Parahumans(pnts);
			break;
		case "Sense Powers":
			ab = new Sense_Powers(pnts);
			break;
		case "Sense Element":
			ab = new Sense_Element_E(element, pnts);
			break;
		case "Elemental Combat I":
			ab = new Elemental_Combat_I_E(element, pnts);
			break;
		case "Elemental Combat II": // NOT DONE
			ab = new Elemental_Combat_II_E(element, pnts);
			break;
		case "Slow Target":
			ab = new Slow_Target(pnts);
			break;
		default:
			MAIN.errorMessage("Donald trump peninsula error - " + abilityName);

			return null;
		// Just because the game isn't finished yet and I still haven't made all 151 ability methods:
		}

		ab.init();

		return ab;
	}
}
